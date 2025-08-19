package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.Content.ContentJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 내 피드 조회 api 통합 테스트")
class FeedShowMineApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private ContentJpaRepository contentJpaRepository;

    @Autowired
    private FollowingJpaRepository followingJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private SavedFeedJpaRepository savedFeedJpaRepository;

    @Autowired
    private PostLikeJpaRepository postLikeJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        postLikeJpaRepository.deleteAllInBatch();
        savedFeedJpaRepository.deleteAllInBatch();
        contentJpaRepository.deleteAllInBatch();
        feedJpaRepository.deleteAllInBatch();
        followingJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        aliasJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("내 피드 조회를 요청할 경우, [feedId, 작성일, 책정보, ,,] 의 피드 정보를 최신순으로 정렬해서 반환한다.")
    void feed_show_mine_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity otherUser = userJpaRepository.save(TestEntityFactory.createUser(a0, "otherUser"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 생성 및 생성일 직접 설정
        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(otherUser, book, true, 50, 10, List.of()));      // otherUser가 작성한 피드

        // JPA flush 후, native update 로 created_at 덮어쓰기
        // feed 작성 순서 : f3 -> f2 -> f1 (f1 이 가장 최신)
        feedJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), f1.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), f2.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), f3.getPostId());

        //when //then
        mockMvc.perform(get("/feeds/mine")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(2)))
                /**
                 * 정렬 조건
                 * 내 글 최신순 조회
                 */
                // 1순위: f1
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f1.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[0].contentUrls", hasSize(2)))
                .andExpect(jsonPath("$.data.feedList[0].likeCount", is(10)))
                .andExpect(jsonPath("$.data.feedList[0].commentCount", is(5)))
                // 2순위: f2
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f2.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].contentUrls", hasSize(0)))
                .andExpect(jsonPath("$.data.feedList[1].likeCount", is(50)))
                .andExpect(jsonPath("$.data.feedList[1].commentCount", is(10)));
    }

    @Test
    @DisplayName("내 피드는 [유저 본인이 작성한 글을 최신순] 으로 반환한다.")
    void feed_show_mine_order_latest() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity otherUser = userJpaRepository.save(TestEntityFactory.createUser(a0, "otherUser"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 생성 및 생성일 직접 설정
        LocalDateTime base = LocalDateTime.now();
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        FeedJpaEntity f4 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of()));
        FeedJpaEntity f5 = feedJpaRepository.save(TestEntityFactory.createFeed(otherUser, book, true, 50, 30, List.of()));      // otherUser가 작성한 피드
        FeedJpaEntity f6 = feedJpaRepository.save(TestEntityFactory.createFeed(otherUser, book, true, 50, 30, List.of()));      // otherUser가 작성한 피드
        FeedJpaEntity f7 = feedJpaRepository.save(TestEntityFactory.createFeed(otherUser, book, true, 50, 30, List.of()));      // otherUser가 작성한 피드
        FeedJpaEntity f8 = feedJpaRepository.save(TestEntityFactory.createFeed(otherUser, book, true, 50, 30, List.of()));      // otherUser가 작성한 피드

        // JPA flush 후, native update 로 created_at 덮어쓰기
        // feed 작성 순서 : f8 -> f7 -> f6 -> f5 -> f4 -> f3 -> f2 -> f1 (f1 이 가장 최신)
        feedJpaRepository.flush();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), f1.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), f2.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), f3.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), f4.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), f5.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), f6.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(35)), f7.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), f8.getPostId());

        //when //then
        mockMvc.perform(get("/feeds/mine")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feedList", hasSize(4)))
                /**
                 * 정렬 조건
                 * 내 글 최신순 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f1.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f2.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[2].feedId", is(f3.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[3].feedId", is(f4.getPostId().intValue())));
    }

    @Test
    @DisplayName("request parameter의 cursor 값이 null일 경우, 첫번째 페이지에 해당하는 피드 10개와, nextCursor, last 값을 반환한다.")
    void feed_show_mine_first_page() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 생성 및 생성일 직접 설정
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f4 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 50, 10, List.of()));
        FeedJpaEntity f5 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f6 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f7 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f8 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f9 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f10 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f11 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f12 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));

        // JPA flush 후, native update 로 created_at 덮어쓰기
        // feed 작성 순서 : f12 -> f11 -> ,,, -> f1 순
        feedJpaRepository.flush();

        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), f1.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), f2.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), f3.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), f4.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), f5.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), f6.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(35)), f7.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), f8.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(45)), f9.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f10.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(55)), f11.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(60)), f12.getPostId());

        //when //then
        mockMvc.perform(get("/feeds/mine")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(false)))
                .andExpect(jsonPath("$.data.feedList", hasSize(10)))
                /**
                 * 정렬 조건
                 * 내 글 최신순 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f1.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f2.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[2].feedId", is(f3.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[3].feedId", is(f4.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[4].feedId", is(f5.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[5].feedId", is(f6.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[6].feedId", is(f7.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[7].feedId", is(f8.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[8].feedId", is(f9.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[9].feedId", is(f10.getPostId().intValue())));
    }

    @Test
    @DisplayName("request parameter의 cursor 값이 존재할 경우, 해당 페이지에 해당하는 피드 10개와, nextCursor, last 값을 반환한다.")
    void feed_show_mine_with_cursor() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book

        // 피드 생성 및 생성일 직접 설정 -> 모두 공개 글
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f4 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 50, 10, List.of()));
        FeedJpaEntity f5 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f6 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f7 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f8 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f9 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f10 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f11 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));
        FeedJpaEntity f12 = feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true, 10, 5, List.of("contentUrl1", "contentUrl2")));

        // JPA flush 후, native update 로 created_at 덮어쓰기
        // feed 작성 순서 : f12 -> f11 -> ,,, -> f1 순
        feedJpaRepository.flush();

        LocalDateTime base = LocalDateTime.now();
        LocalDateTime t10 = base.minusMinutes(50);
        LocalDateTime t11 = base.minusMinutes(55);
        LocalDateTime t12 = base.minusMinutes(60);

        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(t10), f10.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(t11), f11.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(t12), f12.getPostId());

        // DB에 저장된 f10의 createdAt 값을 native query 로 조회
        LocalDateTime nextCursorVal = jdbcTemplate.queryForObject(
                "SELECT created_at FROM posts WHERE post_id = ?",
                (rs, rowNum) -> rs.getTimestamp("created_at").toLocalDateTime(), f10.getPostId()
        );
        String nextCursor = nextCursorVal.toString();

        //when //then
        mockMvc.perform(get("/feeds/mine")
                        .requestAttr("userId", me.getUserId())
                        .param("cursor", nextCursor))        // 이전에 f10 까지 조회 -> f10의 createdAt이 커서
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLast", is(true)))
                .andExpect(jsonPath("$.data.feedList", hasSize(2)))
                /**
                 * 정렬 조건
                 * 내 글 최신순 조회
                 */
                .andExpect(jsonPath("$.data.feedList[0].feedId", is(f11.getPostId().intValue())))
                .andExpect(jsonPath("$.data.feedList[1].feedId", is(f12.getPostId().intValue())));
    }
}
