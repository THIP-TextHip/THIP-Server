package konkuk.thip.feed.adapter.in.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        properties = "feed.show.strategy=basic"
)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 피드 전체 조회(최신순 조회) api 통합 테스트 - Cache 어댑터")
class BasicFeedShowAllCacheApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private SavedFeedJpaRepository savedFeedJpaRepository;

    @Autowired
    private PostLikeJpaRepository postLikeJpaRepository;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    @DisplayName("피드 조회를 요청할 경우, [feedId, 작성자 닉네임, ,,] 의 피드 정보를 최신순으로 정렬해서 반환한다.")
    void feed_show_all_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        // 피드 생성
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true));
        savedFeedJpaRepository.save(
                SavedFeedJpaEntity.builder()
                        .userJpaEntity(me)      // me가 f1을 저장하였음
                        .feedJpaEntity(f1)
                        .build()
        );

        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true));
        postLikeJpaRepository.save(
                PostLikeJpaEntity.builder()
                        .userJpaEntity(me)      // me가 f2를 좋아요 하였음
                        .postJpaEntity(f2)
                        .build()
        );

        // 캐싱 데이터 삽입
        Cache topCache = cacheManager.getCache("feedIdTop");
        topCache.put("top100", List.of(f2.getPostId(), f1.getPostId()));

        Cache detailCache = cacheManager.getCache("feedDetail");
        detailCache.put(f1.getPostId(), createDto(f1.getPostId(), me.getUserId(), "me", true));
        detailCache.put(f2.getPostId(), createDto(f2.getPostId(), user1.getUserId(), "user1", true));

        //when //then
        mockMvc.perform(get("/feeds")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(2)))
                /**
                 * 정렬 조건
                 * 내 글 & 다른 모든 유저의 공개 글을 최신순 조회
                 */
                // 1순위: 팔로잉 글 f2
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f2.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[0].creatorNickname", is("user1")))
                .andExpect(jsonPath("$.data.feedList[0].isSaved", is(false)))
                .andExpect(jsonPath("$.data.feedList[0].isLiked", is(true)))
                // 2순위: 내 글 f1
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f1.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].creatorNickname", is("me")))
                .andExpect(jsonPath("$.data.feedList[1].isSaved", is(true)))
                .andExpect(jsonPath("$.data.feedList[1].isLiked", is(false)));
    }

    @Test
    @DisplayName("피드는 [유저 본인이 작성한 글, 다른 모든 유저가 작성한 공개 글을 최신순] 으로 반환한다.")
    void feed_show_with_priority_and_order() throws Exception {
        //given
        Long myId = 999L; Long user1Id = 1L; Long user2Id = 2L; Long user3Id = 3L;

        Long f1Id = 1L;  Long f2Id = 2L;  Long f3Id = 3L;
        Long f4Id = 4L;  Long f5Id = 5L;  Long f6Id = 6L;
        Long f7Id = 7L;  Long f8Id = 8L;  Long f9Id = 9L;
        Long f10Id = 10L; Long f11Id = 11L; Long f12Id = 12L; Long f13Id = 13L; Long f14Id = 14L;

        Cache topCache = cacheManager.getCache("feedIdTop");
        topCache.put("top100",
                List.of(
                        f14Id, f13Id, f12Id, f11Id, f10Id,
                        f9Id, f8Id, f7Id, f6Id, f5Id,
                        f4Id, f3Id, f2Id, f1Id
                )
        );

        // 피드 생성 -> 비공개 글: f3, f5, f9, f12
        // feed 작성 순서 : f1 -> ... f14 (f14가 가장 최신)
        Cache detailCache = cacheManager.getCache("feedDetail");
        detailCache.put(f1Id, createDto(f1Id, myId, "me", true));
        detailCache.put(f2Id, createDto(f2Id, user1Id, "user1", true));
        detailCache.put(f3Id, createDto(f3Id, user1Id, "user1", false)); // 비공개
        detailCache.put(f4Id, createDto(f4Id, user2Id, "user2", true));
        detailCache.put(f5Id, createDto(f5Id, user2Id, "user2", false)); // 비공개
        detailCache.put(f6Id, createDto(f6Id, user3Id, "user3", true));
        detailCache.put(f7Id, createDto(f7Id, user1Id, "user1", true));
        detailCache.put(f8Id, createDto(f8Id, user2Id, "user2", true));
        detailCache.put(f9Id, createDto(f9Id, user3Id, "user3", false)); // 비공개
        detailCache.put(f10Id, createDto(f10Id, user1Id, "user1", true));
        detailCache.put(f11Id, createDto(f11Id, user2Id, "user2", true));
        detailCache.put(f12Id, createDto(f12Id, user3Id, "user3", false)); // 비공개
        detailCache.put(f13Id, createDto(f13Id, user1Id, "user1", true));
        detailCache.put(f14Id, createDto(f14Id, user2Id, "user2", true));

        //when //then
        mockMvc.perform(get("/feeds")
                        .requestAttr("userId", myId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(10)))
                /**
                 * 정렬 조건
                 * 내 글 & 다른 모든 유저의 공개 글을 최신순 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f14Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f13Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[2].feedId", is(f11Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[3].feedId", is(f10Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[4].feedId", is(f8Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[5].feedId", is(f7Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[6].feedId", is(f6Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[7].feedId", is(f4Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[8].feedId", is(f2Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[9].feedId", is(f1Id.intValue())));

    }

    @Test
    @DisplayName("request parameter의 cursor 값이 null일 경우, 첫번째 페이지에 해당하는 피드 10개와, nextCursor, last 값을 반환한다.")
    void feed_show_first_page() throws Exception {
        //given
        Long myId = 999L; Long user1Id = 1L; Long user2Id = 2L; Long user3Id = 3L;

        Long f1Id = 1L;  Long f2Id = 2L;  Long f3Id = 3L;
        Long f4Id = 4L;  Long f5Id = 5L;  Long f6Id = 6L;
        Long f7Id = 7L;  Long f8Id = 8L;  Long f9Id = 9L;
        Long f10Id = 10L; Long f11Id = 11L; Long f12Id = 12L;

        Cache topCache = cacheManager.getCache("feedIdTop");
        topCache.put("top100",
                List.of(
                        f12Id, f11Id, f10Id, f9Id,
                        f8Id, f7Id, f6Id, f5Id,
                        f4Id, f3Id, f2Id, f1Id
                )
        );

        // 피드 생성 및 생성일 직접 설정 -> 모두 공개 글
        // feed 작성 순서 : f1 -> f2 -> ... f12
        Cache detailCache = cacheManager.getCache("feedDetail");
        detailCache.put(f1Id, createDto(f1Id, myId, "me", true));
        detailCache.put(f2Id, createDto(f2Id, user1Id, "user1", true));
        detailCache.put(f3Id, createDto(f3Id, user1Id, "user1", true));
        detailCache.put(f4Id, createDto(f4Id, user2Id, "user2", true));
        detailCache.put(f5Id, createDto(f5Id, user2Id, "user2", true));
        detailCache.put(f6Id, createDto(f6Id, user3Id, "user3", true));
        detailCache.put(f7Id, createDto(f7Id, user1Id, "user1", true));
        detailCache.put(f8Id, createDto(f8Id, user2Id, "user2", true));
        detailCache.put(f9Id, createDto(f9Id, user3Id, "user3", true));
        detailCache.put(f10Id, createDto(f10Id, user1Id, "user1", true));
        detailCache.put(f11Id, createDto(f11Id, user2Id, "user2", true));
        detailCache.put(f12Id, createDto(f12Id, user3Id, "user3", true));

        //when //then
        mockMvc.perform(get("/feeds")
                        .requestAttr("userId", myId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextCursor", notNullValue()))
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.feedList", hasSize(10)))
                /**
                 * 정렬 조건
                 * 내 글 & 다른 모든 유저의 공개 글을 최신순 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f12Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f11Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[2].feedId", is(f10Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[3].feedId", is(f9Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[4].feedId", is(f8Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[5].feedId", is(f7Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[6].feedId", is(f6Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[7].feedId", is(f5Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[8].feedId", is(f4Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[9].feedId", is(f3Id.intValue())));
    }

    @Test
    @DisplayName("request parameter의 cursor 값이 존재할 경우, 해당 페이지에 해당하는 피드 10개와, nextCursor, last 값을 반환한다.")
    void feed_show_with_cursor() throws Exception {
        //given
        Long myId = 999L; Long user1Id = 1L;

        Long f1Id = 1L;   Long f2Id = 2L;   Long f3Id = 3L;   Long f4Id = 4L;   Long f5Id = 5L;
        Long f6Id = 6L;   Long f7Id = 7L;   Long f8Id = 8L;   Long f9Id = 9L;   Long f10Id = 10L;
        Long f11Id = 11L; Long f12Id = 12L; Long f13Id = 13L; Long f14Id = 14L; Long f15Id = 15L;
        Long f16Id = 16L; Long f17Id = 17L; Long f18Id = 18L; Long f19Id = 19L; Long f20Id = 20L;

        Cache topCache = cacheManager.getCache("feedIdTop");
        topCache.put("top100",
                List.of(
                        f20Id, f19Id, f18Id, f17Id, f16Id,
                        f15Id, f14Id, f13Id, f12Id, f11Id,
                        f10Id, f9Id, f8Id, f7Id, f6Id,
                        f5Id, f4Id, f3Id, f2Id, f1Id
                )
        );

        // 피드 생성 및 생성일 직접 설정 -> 모두 공개 글
        // feed 작성 순서 : f1 -> f2 -> ... f20
        Cache detailCache = cacheManager.getCache("feedDetail");
        List<Long> allIds = List.of(
                f1Id,f2Id,f3Id,f4Id,f5Id,f6Id,f7Id,f8Id,f9Id,f10Id,
                f11Id,f12Id,f13Id,f14Id,f15Id,f16Id,f17Id,f18Id,f19Id,f20Id
        );
        for (Long id : allIds) {
            detailCache.put(id, createDto(id, user1Id, "user", true));
        }

        String nextCursor = f11Id.toString();

        //when //then
        mockMvc.perform(get("/feeds")
                        .requestAttr("userId", myId)
                        .param("cursor", nextCursor))        // 이전에 f11 까지 조회 -> 11의 postId가 커서
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextCursor", nullValue()))      // nextCursor 는 null
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.feedList", hasSize(10)))
                /**
                 * 정렬 조건
                 * 내 글 & 다른 모든 유저의 공개 글을 최신순 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f10Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f9Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[2].feedId", is(f8Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[3].feedId", is(f7Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[4].feedId", is(f6Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[5].feedId", is(f5Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[6].feedId", is(f4Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[7].feedId", is(f3Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[8].feedId", is(f2Id.intValue())))
                .andExpect(jsonPath("$.data.feedList[9].feedId", is(f1Id.intValue())));
    }

    private FeedQueryDto createDto(Long feedId, Long creatorId, String nickname, boolean isPublic) {
        return FeedQueryDto.builder()
                .feedId(feedId)
                .creatorId(creatorId)
                .creatorNickname(nickname)
                .creatorProfileImageUrl("/profile_science.png")
                .alias("과학자")
                .createdAt(LocalDateTime.now())
                .isbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13))
                .bookTitle("테스트 책")
                .bookAuthor("테스트 저자")
                .contentBody("기본 피드 본문입니다.")
                .contentUrls(new String[]{})
                .likeCount(0)
                .commentCount(0)
                .isPublic(isPublic)
                .isPriorityFeed(false)
                .savedCreatedAt(null)
                .build();
    }
}
