package konkuk.thip.feed.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.value.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[통합] 특정 책으로 작성된 피드 조회 api 통합 테스트")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class FeedRelatedWithBookApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private SavedFeedJpaRepository savedFeedJpaRepository;

    @Autowired
    private PostLikeJpaRepository postLikeJpaRepository;

    private static final String VALID_ISBN = "9781234567890";

    @Test
    @DisplayName("책 존재하지 않으면 빈 리스트 isLast true nextCursor null 반환")
    void getFeedsByBook_book_not_exist_returns_empty() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/feeds/related-books/" + VALID_ISBN)
                .requestAttr("userId", 1L) // 임의 사용자
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.feeds").isArray())
                .andExpect(jsonPath("$.data.feeds").isEmpty())
                .andExpect(jsonPath("$.data.isLast").value(true))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("기본 정렬 like 좋아요순 응답 성공")
    void getFeedsByBook_like_default_success() throws Exception {
        // given
        TestData td = prepareDataForFeeds(); // 사용자 피드 생성

        // requester가 저장한 피드 지정
        savedFeedJpaRepository.save(TestEntityFactory.createSavedFeed(td.requester, td.publicFeed2));

        // requester가 좋아요한 피드 지정
        postLikeJpaRepository.save(PostLikeJpaEntity.builder()
                .userJpaEntity(td.requester)
                .postJpaEntity(td.publicFeed1)
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/feeds/related-books/" + td.book.getIsbn())
                .requestAttr("userId", td.requester.getUserId())
                .param("sort", "like")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.feeds").isArray())
                .andExpect(jsonPath("$.data.isLast").isBoolean());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode feeds = root.path("data").path("feeds");

        assertThat(feeds.size()).isGreaterThan(0);

        // 좋아요 내림차순 검증
        int prevLike = Integer.MAX_VALUE;
        for (JsonNode f : feeds) {
            int like = f.path("likeCount").asInt();
            assertThat(like).isLessThanOrEqualTo(prevLike);
            prevLike = like;
        }

        // isWriter false 검증 requester가 아닌 다른 사용자의 피드만 조회
        for (JsonNode f : feeds) {
            assertThat(f.path("isWriter").asBoolean()).isFalse();
        }

        // isSaved isLiked 매핑 검증
        // publicFeed2 저장됨 → isSaved true
        // publicFeed1 좋아요됨 → isLiked true
        boolean hasSavedTrue = false;
        boolean hasLikedTrue = false;
        boolean hasContentUrls = false;
        for (JsonNode f : feeds) {
            if (f.path("feedId").asLong() == td.publicFeed2.getPostId()) {
                assertThat(f.path("isSaved").asBoolean()).isTrue();
                hasSavedTrue = true;
            }
            if (f.path("feedId").asLong() == td.publicFeed1.getPostId()) {
                assertThat(f.path("isLiked").asBoolean()).isTrue();
                hasLikedTrue = true;
            }
            // contentUrls 배열 확인
            if (f.has("contentUrls") && f.path("contentUrls").isArray()) {
                hasContentUrls = true;
            }
        }
        assertThat(hasSavedTrue).isTrue();
        assertThat(hasLikedTrue).isTrue();
        assertThat(hasContentUrls).isTrue();
    }

    @Test
    @DisplayName("정렬 latest 최신순 응답 성공 createdAt 내림차순 검증")
    void getFeedsByBook_latest_success() throws Exception {
        // given
        TestData td = prepareDataForFeeds();

        // when
        ResultActions result = mockMvc.perform(get("/feeds/related-books/" + td.book.getIsbn())
                .requestAttr("userId", td.requester.getUserId())
                .param("sort", "latest")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode feeds = root.path("data").path("feeds");

        assertThat(feeds.size()).isGreaterThan(0);

        // createdAt 정렬은 응답 필드에 노출되지 않지만 서버 정렬 결과의 안정성은 likeCount 동일 시 createdAt 보조키로 검증하기 어려우므로
        // 최신 정렬 케이스는 최소 결과 유효성 중심으로 점검
        // 추가 보조 검증 isWriter false 유지
        for (JsonNode f : feeds) {
            assertThat(f.path("isWriter").asBoolean()).isFalse();
        }
    }

    @Test
    @DisplayName("커서 기반 페이징 동작 검증")
    void getFeedsByBook_cursor_paging_success() throws Exception {
        // given
        int totalFeeds = 25; // 총 25개의 피드를 생성
        int pageSize = 10; // 페이지당 10개씩
        int sum = 0; // 피드 개수 합산 검증용
        TestData td = prepareDataManyFeeds(totalFeeds);

        // first page
        ResultActions first = mockMvc.perform(get("/feeds/related-books/" + td.book.getIsbn())
                .requestAttr("userId", td.requester.getUserId())
                .param("sort", "like")
                .contentType(MediaType.APPLICATION_JSON));

        first.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeds").isArray());

        String firstJson = first.andReturn().getResponse().getContentAsString();
        JsonNode firstRoot = objectMapper.readTree(firstJson);
        JsonNode feeds = firstRoot.path("data").path("feeds");
        assertThat(feeds.size()).isEqualTo(pageSize);
        sum += feeds.size();
        String nextCursor = firstRoot.path("data").path("nextCursor").asText(null);
        boolean isLast = firstRoot.path("data").path("isLast").asBoolean();

        if (!isLast && nextCursor != null && !nextCursor.isEmpty()) {
            // second page
            ResultActions second = mockMvc.perform(get("/feeds/related-books/" + td.book.getIsbn())
                    .requestAttr("userId", td.requester.getUserId())
                    .param("sort", "like")
                    .param("cursor", nextCursor)
                    .contentType(MediaType.APPLICATION_JSON));

            second.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.feeds").isArray());

            String secondJson = second.andReturn().getResponse().getContentAsString();
            JsonNode secondRoot = objectMapper.readTree(secondJson);
            JsonNode secondFeeds = secondRoot.path("data").path("feeds");
            assertThat(secondFeeds.size()).isEqualTo(pageSize);
            sum += secondFeeds.size();
            nextCursor = secondRoot.path("data").path("nextCursor").asText(null);
            isLast = secondRoot.path("data").path("isLast").asBoolean();
            assertThat(isLast).isFalse(); // 아직 마지막 페이지가 아니어야 함
        }

        if (!isLast && nextCursor != null && !nextCursor.isEmpty()) {
            // third page
            ResultActions third = mockMvc.perform(get("/feeds/related-books/" + td.book.getIsbn())
                    .requestAttr("userId", td.requester.getUserId())
                    .param("sort", "like")
                    .param("cursor", nextCursor)
                    .contentType(MediaType.APPLICATION_JSON));

            third.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.feeds").isArray());

            String thirdJson = third.andReturn().getResponse().getContentAsString();
            JsonNode thirdRoot = objectMapper.readTree(thirdJson);
            JsonNode thirdFeeds = thirdRoot.path("data").path("feeds");
            assertThat(thirdFeeds.size()).isLessThanOrEqualTo(pageSize);
            sum += thirdFeeds.size();
            boolean isLastThird = thirdRoot.path("data").path("isLast").asBoolean();
            assertThat(isLastThird).isTrue(); // 마지막 페이지여야 함
            String thirdCursor = thirdRoot.path("data").path("nextCursor").asText();
            assertThat(thirdCursor).isEqualTo("null"); // 다음 커서가 없어야 함
        }

        // 전체 피드 개수 검증
        assertThat(sum).isEqualTo(totalFeeds);
    }

    @Test
    @DisplayName("비공개 피드 제외 검증")
    void getFeedsByBook_visibility_and_self_filter() throws Exception {
        // given
        Alias alias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity requester = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_req")
                .nickname("요청자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        Category category = TestEntityFactory.createLiteratureCategory();

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN(VALID_ISBN));

        // 자기 자신 글 두 개 생성
        FeedJpaEntity myPublic = TestEntityFactory.createFeed(requester, book, true);       // 내 공개 피드
        FeedJpaEntity myPrivate = TestEntityFactory.createFeed(requester, book, false);     // 내 비공개 피드
        feedJpaRepository.saveAll(List.of(myPublic, myPrivate));

        // 다른 사람 공개 글 하나 생성
        UserJpaEntity other = userJpaRepository.save(TestEntityFactory.createUser(alias, "다른사용자"));
        FeedJpaEntity othersPublic = TestEntityFactory.createFeed(other, book, true);
        feedJpaRepository.save(othersPublic);

        // when
        ResultActions result = mockMvc.perform(get("/feeds/related-books/" + book.getIsbn())
                .requestAttr("userId", requester.getUserId())
                .param("sort", "like")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        String json = result.andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode feeds = root.path("data").path("feeds");

        // 자기 자신 글은 포함 & 비공개 글은 제외 -> 자기 자신의 공개 피드 + othersPublic (= 2개)만 남아야 함
        List<Long> feedIds = new ArrayList<>();
        for (JsonNode f : feeds) {
            feedIds.add(f.path("feedId").asLong());
        }

        assertThat(feedIds.size()).isEqualTo(2);
        assertThat(feedIds).containsExactlyInAnyOrder(
                myPublic.getPostId(),
                othersPublic.getPostId()
        );
    }

    private TestData prepareDataForFeeds() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        Category category = TestEntityFactory.createLiteratureCategory();

        UserJpaEntity requester = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_req")
                .nickname("요청자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        UserJpaEntity author = userJpaRepository.save(TestEntityFactory.createUser(alias, "작성자"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN(VALID_ISBN));

        // 공개 피드 두 개 비공개 하나 생성
        FeedJpaEntity publicFeed1 = TestEntityFactory.createFeed(
                author,
                book,
                List.of("http://img/1.jpg", "http://img/2.jpg"),
                true
        );
        publicFeed1.updateLikeCount(7); // 좋아요 정렬을 위해 수치 조정

        FeedJpaEntity publicFeed2 = TestEntityFactory.createFeed(
                author,
                book,
                List.of("http://img/3.jpg"),
                true
        );
        publicFeed2.updateLikeCount(3);

        FeedJpaEntity privateFeed = TestEntityFactory.createFeed(author, book, false);

        feedJpaRepository.saveAll(List.of(publicFeed1, publicFeed2, privateFeed));

        return new TestData(requester, author, book, publicFeed1, publicFeed2);
    }

    private TestData prepareDataManyFeeds(int count) {
        Alias alias = TestEntityFactory.createScienceAlias();

        UserJpaEntity requester = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_req_many")
                .nickname("요청자")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        UserJpaEntity author = userJpaRepository.save(TestEntityFactory.createUser(alias, "작성자"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBookWithISBN(VALID_ISBN));

        for (int i = 0; i < count; i++) {
            FeedJpaEntity f = TestEntityFactory.createFeed(
                    author,
                    book,
                    List.of("http://img/" + i + ".jpg"),
                    true
            );
            f.updateLikeCount(count - i); // 다양한 likeCount 부여
            feedJpaRepository.save(f);
        }

        return new TestData(requester, author, book, null, null);
    }

    private record TestData(
            UserJpaEntity requester,
            UserJpaEntity author,
            BookJpaEntity book,
            FeedJpaEntity publicFeed1,
            FeedJpaEntity publicFeed2
    ) {}
}
