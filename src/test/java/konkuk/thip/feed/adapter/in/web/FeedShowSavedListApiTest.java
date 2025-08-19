package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 저장한 피드 조회 api 통합 테스트")
class FeedShowSavedListApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private SavedFeedJpaRepository savedFeedJpaRepository;
    @Autowired private PostLikeJpaRepository postLikeJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("저장된 피드 조회 시 피드 정보를 피드를 저장한 최신순으로 정렬해서 반환한다.")
    void saved_feed_show_test_success() throws Exception {
        // given
        Alias alias = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(alias, "user1"));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        // 피드 생성
        LocalDateTime baseTime = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(
                TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f2 = feedJpaRepository.save(
                TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));
        // me가 f2를 좋아요
        postLikeJpaRepository.save(
                PostLikeJpaEntity.builder()
                        .userJpaEntity(me)
                        .postJpaEntity(f2)
                        .build()
        );

        // 저장 시연 - me가 f1 저장
        SavedFeedJpaEntity sf1 = savedFeedJpaRepository.save(SavedFeedJpaEntity.builder()
                .userJpaEntity(me)
                .feedJpaEntity(f1)
                .build());

        // 저장 시연 - me가 f2 저장 (조금 더 이전)
        SavedFeedJpaEntity sf2 = savedFeedJpaRepository.save(SavedFeedJpaEntity.builder()
                .userJpaEntity(me)
                .feedJpaEntity(f2)
                .build());

        // flush 후 feed 저장일자 덮어쓰기
        // feed 저장 순서 : f2 -> f1 (f1 이 가장 최신)
        savedFeedJpaRepository.flush();
        jdbcTemplate.update("UPDATE saved_feeds SET created_at = ? WHERE saved_id = ?",
                Timestamp.valueOf(baseTime.minusMinutes(1)), sf1.getSavedId());
        jdbcTemplate.update("UPDATE saved_feeds SET created_at = ? WHERE saved_id = ?",
                Timestamp.valueOf(baseTime.minusMinutes(10)), sf2.getSavedId());

        // when & then
        mockMvc.perform(get("/feeds/saved")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(2)))
                // 저장한 최신순 -> f1 먼저
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f1.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[0].creatorNickname", is("me")))
                .andExpect(jsonPath("$.data.feedList[0].contentUrls", hasSize(2)))
                .andExpect(jsonPath("$.data.feedList[0].likeCount", is(10)))
                .andExpect(jsonPath("$.data.feedList[0].commentCount", is(5)))
                .andExpect(jsonPath("$.data.feedList[0].isSaved", is(true)))
                .andExpect(jsonPath("$.data.feedList[0].isLiked", is(false)))
                // 두 번째는 f2
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f2.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].creatorNickname", is("user1")))
                .andExpect(jsonPath("$.data.feedList[1].contentUrls", hasSize(0)))
                .andExpect(jsonPath("$.data.feedList[1].likeCount", is(50)))
                .andExpect(jsonPath("$.data.feedList[1].commentCount", is(10)))
                .andExpect(jsonPath("$.data.feedList[1].isSaved", is(true)))
                .andExpect(jsonPath("$.data.feedList[1].isLiked", is(true)));
    }

    @Test
    @DisplayName("request parameter의 cursor 값이 null일 경우, 첫번째 페이지에 해당하는 피드 10개와, nextCursor, last 값을 반환한다.")
    void saved_feed_show_with_first_page() throws Exception {

        // given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        // 피드 12개 생성 및 저장
        LocalDateTime baseTime = LocalDateTime.now();
        FeedJpaEntity[] feeds = new FeedJpaEntity[12];
        SavedFeedJpaEntity[] savedFeeds = new SavedFeedJpaEntity[12];

        for (int i = 0; i < 12; i++) {
            FeedJpaEntity feed = feedJpaRepository.save(
                    TestEntityFactory.createFeed(me, book, true, 10 + i, 5 + i, List.of("contentUrl" + i)));
            feeds[i] = feed;
            SavedFeedJpaEntity savedFeed = savedFeedJpaRepository.save(
                    SavedFeedJpaEntity.builder()
                            .userJpaEntity(me)
                            .feedJpaEntity(feed)
                            .build()
            );
            savedFeeds[i] = savedFeed;
        }
        savedFeedJpaRepository.flush();

        // created_at 덮어쓰기 feedId가 작을수록 최신 저장순
         for (int i = 0; i < 12; i++) {
            jdbcTemplate.update("UPDATE saved_feeds SET created_at = ? WHERE saved_id = ?",
                Timestamp.valueOf(baseTime.minusMinutes(i)), savedFeeds[i].getSavedId());
         }

        // when & then
        mockMvc.perform(get("/feeds/saved")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(10)))
                .andExpect(jsonPath("$.data.nextCursor", notNullValue()))
                .andExpect(jsonPath("$.data.isLast", is(false)))
                /**
                 * 정렬 조건
                 * 저장한 피드를 저장한 순으로 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(feeds[0].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(feeds[1].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[2].feedId", is(feeds[2].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[3].feedId", is(feeds[3].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[4].feedId", is(feeds[4].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[5].feedId", is(feeds[5].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[6].feedId", is(feeds[6].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[7].feedId", is(feeds[7].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[8].feedId", is(feeds[8].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[9].feedId", is(feeds[9].getPostId().intValue())));
    }

    @Test
    @DisplayName("request parameter의 cursor 값이 존재할 경우, 해당 페이지에 해당하는 피드 10개와, nextCursor, last 값을 반환한다.")
    void saved_feed_show_with_cursor() throws Exception {
        // given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());

        // 피드 12개 생성 및 저장
        LocalDateTime baseTime = LocalDateTime.now();
        FeedJpaEntity[] feeds = new FeedJpaEntity[12];
        SavedFeedJpaEntity[] savedFeeds = new SavedFeedJpaEntity[12];

        for (int i = 0; i < 12; i++) {
            FeedJpaEntity feed = feedJpaRepository.save(
                    TestEntityFactory.createFeed(me, book, true, 10 + i, 5 + i, List.of("contentUrl" + i)));
            feeds[i] = feed;
            SavedFeedJpaEntity savedFeed = savedFeedJpaRepository.save(
                    SavedFeedJpaEntity.builder()
                            .userJpaEntity(me)
                            .feedJpaEntity(feed)
                            .build()
            );
            savedFeeds[i] = savedFeed;
        }
        savedFeedJpaRepository.flush();

        // created_at 덮어쓰기 feedId가 작을수록 최신 저장순
        for (int i = 0; i < 12; i++) {
            jdbcTemplate.update("UPDATE saved_feeds SET created_at = ? WHERE saved_id = ?",
                    Timestamp.valueOf(baseTime.minusMinutes(i)), savedFeeds[i].getSavedId());
        }

        // DB에 저장된 sf9의 createdAt 값을 native query 로 조회
        LocalDateTime nextCursorVal = jdbcTemplate.queryForObject(
                "SELECT created_at FROM saved_feeds WHERE saved_id = ?",
                (rs, rowNum) -> rs.getTimestamp("created_at").toLocalDateTime(), savedFeeds[9].getSavedId()
        );
        String nextCursor = nextCursorVal.toString();

        //when //then
        mockMvc.perform(get("/feeds/saved")
                        .requestAttr("userId", me.getUserId())
                        .param("cursor", nextCursor))        // 이전에 f9 까지 조회 -> f9의 저장시간, sf9의 createdAt이 커서
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextCursor", nullValue()))      // nextCursor 는 null
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.feedList", hasSize(2)))
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(feeds[10].getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(feeds[11].getPostId().intValue())));
    }

//    @Test
//    @DisplayName("[깨짐 재현] 최신 저장 피드에 contents가 많으면 첫 페이지 결과 개수가 10개보다 적게 반환된다")
//    void saved_feed_paging_breaks_with_many_contents() throws Exception {
//        // given
//        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
//        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias, "me"));
//        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
//
//        LocalDateTime baseTime = LocalDateTime.now();
//
//        // 피드 12개 생성. 가장 최신(f0)과 그 다음(f1, f2)에 많은 contents 부여 → row 폭발 유도
//        FeedJpaEntity[] feeds = new FeedJpaEntity[12];
//        SavedFeedJpaEntity[] savedFeeds = new SavedFeedJpaEntity[12];
//
//        for (int i = 0; i < 12; i++) {
//            List<String> contentUrls;
//            if (i == 0) {
//                // 최신 저장된 피드: 컨텐츠 3개
//                contentUrls = java.util.stream.IntStream.range(0, 3)
//                        .mapToObj(n -> "url_f0_" + n)
//                        .toList();
//            } else if (i == 1) {
//                // 두 번째: 컨텐츠 3개
//                contentUrls = java.util.stream.IntStream.range(0, 3)
//                        .mapToObj(n -> "url_f1_" + n)
//                        .toList();
//            } else if (i == 2) {
//                // 세 번째: 컨텐츠 2개
//                contentUrls = java.util.stream.IntStream.range(0, 2)
//                        .mapToObj(n -> "url_f2_" + n)
//                        .toList();
//            } else {
//                // 나머지: 컨텐츠 0개
//                contentUrls = List.of();
//            }
//
//            FeedJpaEntity feed = feedJpaRepository.save(
//                    TestEntityFactory.createFeed(
//                            me,                       // 작성자: me (가시성 조건 단순화)
//                            book,
//                            true,                     // 공개
//                            10 + i,                   // likeCount
//                            5 + i,                    // commentCount
//                            contentUrls
//                    )
//            );
//            feeds[i] = feed;
//
//            SavedFeedJpaEntity saved = savedFeedJpaRepository.save(
//                    SavedFeedJpaEntity.builder()
//                            .userJpaEntity(me)
//                            .feedJpaEntity(feed)
//                            .build()
//            );
//            savedFeeds[i] = saved;
//        }
//        savedFeedJpaRepository.flush();
//
//        // created_at 덮어쓰기: i가 작을수록 더 최신 (f0가 가장 최신)
//        for (int i = 0; i < 12; i++) {
//            jdbcTemplate.update("UPDATE saved_feeds SET created_at = ? WHERE saved_id = ?",
//                    Timestamp.valueOf(baseTime.minusMinutes(i)), savedFeeds[i].getSavedId());
//        }
//
//        // when & then
//        mockMvc.perform(get("/feeds/saved")
//                        .requestAttr("userId", me.getUserId()))
//                .andExpect(status().isOk())
//                // ⚠️ 현재 구현(컬렉션 fetch join + limit)에서는 행 기준으로 잘려 루트 엔티티 개수가 모자라게 반환됨
//                .andExpect(jsonPath("$.data.feedList", hasSize(10)))    // 총 응답개수는 10개가 맞음
//                .andExpect(result -> {
//                    String body = result.getResponse().getContentAsString();
//                    // com.jayway.jsonpath.JsonPath 사용 (spring-boot-starter-test에 포함)
//                    List<Integer> ids = com.jayway.jsonpath.JsonPath.read(body, "$.data.feedList[*].feedId");
//                    Set<Integer> distinct = new HashSet<>(ids);
//                    // 중복이 있음을 기대(= 실패를 통해 버그 재현). 만약 여기서 실패하면 중복이 안 나온 것.
//                    assertThat(distinct.size())
//                            .as("feedId가 중복 없이 10개 모두 달라야 하지만, 1:N 조인으로 인해 중복이 발생해야 합니다.")
//                            .isLessThan(ids.size());
//                });
//    }
}
