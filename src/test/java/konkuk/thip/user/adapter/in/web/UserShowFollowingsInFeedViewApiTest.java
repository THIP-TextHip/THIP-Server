package konkuk.thip.user.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.domain.value.Alias;
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
@DisplayName("[통합] 피드 조회 화면에서, 내 띱 목록 조회 api 통합 테스트")
class UserShowFollowingsInFeedViewApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private FeedJpaRepository feedJpaRepository;
    @Autowired private FollowingJpaRepository followingJpaRepository;
    @Autowired private BookJpaRepository bookJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        feedJpaRepository.deleteAllInBatch();
        followingJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("전체 피드 조회 화면에서, 내가 팔로잉 하는 사람들의 [userId, 닉네임, 프로필 이미지] 정보를 1.최근 공개 피드를 작성한 사람 -> 2.최근 팔로잉 맺은 사람 순으로 반환합니다.")
    void show_my_following_recent_writers_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));
        UserJpaEntity user3 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user3"));

        // me가 user1, user2를 팔로잉하였음 (user3는 팔로잉하지 X)
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user1));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user2));

        // user1, user3이 공개 피드를 작성
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(user3, book, true, 50, 10, List.of()));

        //when //then
        mockMvc.perform(get("/users/my-followings/recent-feeds")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myFollowingUsers", hasSize(2)))
                /**
                 * 정렬 조건 : 내가 팔로잉하는 사람들 중,
                 * 1. 최근에 공개 피드를 작성한 사람
                 * 2. 최근에 팔로잉을 맺은 사람
                 * : user1 -> user2 순
                 */
                .andExpect(jsonPath("$.data.myFollowingUsers[0].userId", is(user1.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[0].nickname", is(user1.getNickname())))
                .andExpect(jsonPath("$.data.myFollowingUsers[0].profileImageUrl", is(a0.getImageUrl())))
                .andExpect(jsonPath("$.data.myFollowingUsers[1].userId", is(user2.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[1].nickname", is(user2.getNickname())))
                .andExpect(jsonPath("$.data.myFollowingUsers[1].profileImageUrl", is(a0.getImageUrl())));
    }

    @Test
    @DisplayName("내가 팔로잉 하는 사람들 중, 공개 피드를 작성한 사람들이 많을 경우, 이들 중 가장 최근에 피드를 작성한 사람들 10명만을 조회합니다.")
    void show_my_following_recent_writers_private_feed_writer_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();

        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));
        UserJpaEntity user3 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user3"));
        UserJpaEntity user4 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user4"));
        UserJpaEntity user5 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user5"));
        UserJpaEntity user6 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user6"));
        UserJpaEntity user7 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user7"));
        UserJpaEntity user8 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user8"));
        UserJpaEntity user9 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user9"));
        UserJpaEntity user10 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user10"));
        UserJpaEntity user11 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user11"));
        UserJpaEntity user12 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user12"));

        // me가 모든 유저 팔로잉 하였음
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user1));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user2));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user3));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user4));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user5));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user6));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user7));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user8));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user9));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user10));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user11));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user12));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(user2, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(user3, book, true, 50, 10, List.of()));
        FeedJpaEntity f4 = feedJpaRepository.save(TestEntityFactory.createFeed(user4, book, true, 50, 10, List.of()));
        FeedJpaEntity f5 = feedJpaRepository.save(TestEntityFactory.createFeed(user5, book, true, 50, 10, List.of()));
        FeedJpaEntity f6 = feedJpaRepository.save(TestEntityFactory.createFeed(user6, book, true, 50, 10, List.of()));
        FeedJpaEntity f7 = feedJpaRepository.save(TestEntityFactory.createFeed(user7, book, true, 50, 10, List.of()));
        FeedJpaEntity f8 = feedJpaRepository.save(TestEntityFactory.createFeed(user8, book, true, 50, 10, List.of()));
        FeedJpaEntity f9 = feedJpaRepository.save(TestEntityFactory.createFeed(user9, book, true, 50, 10, List.of()));
        FeedJpaEntity f10 = feedJpaRepository.save(TestEntityFactory.createFeed(user10, book, true, 50, 10, List.of()));
        FeedJpaEntity f11 = feedJpaRepository.save(TestEntityFactory.createFeed(user11, book, true, 50, 10, List.of()));
        FeedJpaEntity f12 = feedJpaRepository.save(TestEntityFactory.createFeed(user12, book, true, 50, 10, List.of()));

        // 피드 작성 순서 : user1이 작성한 f1 -> user2가 작성한 f2 -> ,,, -> f12
        feedJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(60)), f1.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(55)), f2.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f3.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(45)), f4.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), f5.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(35)), f6.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), f7.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), f8.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), f9.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), f10.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), f11.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), f12.getPostId());

        //when //then
        mockMvc.perform(get("/users/my-followings/recent-feeds")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myFollowingUsers", hasSize(10)))
                /**
                 * 정렬 조건 : 내가 팔로잉하는 사람들 중,
                 * 1. 최근에 공개 피드를 작성한 사람
                 * 2. 최근에 팔로잉을 맺은 사람
                 * : user12 -> user11 -> ,, -> user3 순
                 */
                .andExpect(jsonPath("$.data.myFollowingUsers[0].userId", is(user12.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[1].userId", is(user11.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[2].userId", is(user10.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[3].userId", is(user9.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[4].userId", is(user8.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[5].userId", is(user7.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[6].userId", is(user6.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[7].userId", is(user5.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[8].userId", is(user4.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[9].userId", is(user3.getUserId().intValue())));
    }

    @Test
    @DisplayName("내가 팔로잉 하는 사람들 중, 최근에 공개 피드를 작성한 사람이 적을 경우, 이 사람들 + 최근에 팔로잉 맺은 사람들을 10명 조회합니다.")
    void show_my_following_recent_writers_too_many_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();

        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));
        UserJpaEntity user3 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user3"));
        UserJpaEntity user4 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user4"));
        UserJpaEntity user5 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user5"));
        UserJpaEntity user6 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user6"));
        UserJpaEntity user7 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user7"));
        UserJpaEntity user8 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user8"));
        UserJpaEntity user9 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user9"));
        UserJpaEntity user10 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user10"));
        UserJpaEntity user11 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user11"));
        UserJpaEntity user12 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user12"));

        // me가 모든 유저 팔로잉 하였음
        FollowingJpaEntity following1 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user1));
        FollowingJpaEntity following2 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user2));
        FollowingJpaEntity following3 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user3));
        FollowingJpaEntity following4 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user4));
        FollowingJpaEntity following5 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user5));
        FollowingJpaEntity following6 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user6));
        FollowingJpaEntity following7 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user7));
        FollowingJpaEntity following8 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user8));
        FollowingJpaEntity following9 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user9));
        FollowingJpaEntity following10 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user10));
        FollowingJpaEntity following11 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user11));
        FollowingJpaEntity following12 = followingJpaRepository.save(TestEntityFactory.createFollowing(me, user12));

        // user1 ~ user5 만 공개 피드를 작성하였음
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(user2, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(user3, book, true, 50, 10, List.of()));
        FeedJpaEntity f4 = feedJpaRepository.save(TestEntityFactory.createFeed(user4, book, true, 50, 10, List.of()));
        FeedJpaEntity f5 = feedJpaRepository.save(TestEntityFactory.createFeed(user5, book, true, 50, 10, List.of()));

        // 피드 작성 순서 : user1이 작성한 f1 -> user2가 작성한 f2 -> ,,, -> f5
        feedJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(60)), f1.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(55)), f2.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), f3.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(45)), f4.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), f5.getPostId());

        // 팔로잉 맺은 순서 : user12 -> user11 -> ,,, user1
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(60)), following12.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(55)), following11.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(50)), following10.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(45)), following9.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(40)), following8.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(35)), following7.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), following6.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), following5.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), following4.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), following3.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), following2.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), following1.getFollowingId());

        //when //then
        mockMvc.perform(get("/users/my-followings/recent-feeds")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myFollowingUsers", hasSize(10)))
                /**
                 * 정렬 조건 : 내가 팔로잉하는 사람들 중,
                 * 1. 최근에 공개 피드를 작성한 사람 (공개 피드 작성 순서 : user1 -> user2 -> ,,, -> user5)
                 * 2. 최근에 팔로잉을 맺은 사람 (팔로잉 맺은 순서 : user12 -> user11 -> ,,, -> user1)
                 * : {user5 -> user4 -> ,, -> user1} -> {(user1 -> ,, -> user5 는 제외) user6 -> user7 -> ,, -> user10}
                 * = {최근 공개 피드를 작성한 사람 순} -> {최근에 팔로잉 맺은 순, 이때 겹치는 사람이면 제외}
                 */
                .andExpect(jsonPath("$.data.myFollowingUsers[0].userId", is(user5.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[1].userId", is(user4.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[2].userId", is(user3.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[3].userId", is(user2.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[4].userId", is(user1.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[5].userId", is(user6.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[6].userId", is(user7.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[7].userId", is(user8.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[8].userId", is(user9.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[9].userId", is(user10.getUserId().intValue())));
    }

    @Test
    @DisplayName("유저가 가장 최근에 작성한 피드의 작성 시각(= createdAt)을 기준으로 정렬하여 반환된다.")
    void show_my_following_recent_writers_latest_feed_check_test() throws Exception {
        //given
        Alias a0 = TestEntityFactory.createScienceAlias();
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));

        // me가 user1, user2를 팔로잉하였음
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user1));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user2));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(user2, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));

        // 피드 작성 순서 : user1이 작성한 f1 -> user2가 작성한 f2 -> user1가 작성한 f3
        feedJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), f1.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), f2.getPostId());
        jdbcTemplate.update(
                "UPDATE posts SET created_at = ? WHERE post_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), f3.getPostId());

        //when //then
        mockMvc.perform(get("/users/my-followings/recent-feeds")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myFollowingUsers", hasSize(2)))
                /**
                 * 정렬 조건 : 내가 팔로잉하는 사람들 중,
                 * 1. 최근에 공개 피드를 작성한 사람
                 * 2. 최근에 팔로잉을 맺은 사람
                 * : user1 -> user2 -> (user1 는 중복이므로 제외)
                 */
                .andExpect(jsonPath("$.data.myFollowingUsers[0].userId", is(user1.getUserId().intValue())))
                .andExpect(jsonPath("$.data.myFollowingUsers[1].userId", is(user2.getUserId().intValue())));
    }
}
