package konkuk.thip.user.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
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
@DisplayName("[통합] 내가 팔로잉하는 사람들 중, 최근에 피드 작성한 사람들 조회 api 통합 테스트")
class UserShowFollowingRecentWritersApiTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AliasJpaRepository aliasJpaRepository;
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
        aliasJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("내가 팔로잉 하는 사람들 중, 최근에 공개 피드를 작성한 사람들의 [userId, 닉네임, 프로필 이미지] 정보를 반환합니다.")
    void show_my_following_recent_writers_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));
        UserJpaEntity user2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user2"));
        UserJpaEntity user3 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user3"));

        // me가 user1, user2를 팔로잉하였음 (user3는 팔로잉하지 X)
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user1));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user2));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, true, 50, 10, List.of()));
        FeedJpaEntity f2 = feedJpaRepository.save(TestEntityFactory.createFeed(user2, book, true, 50, 10, List.of()));
        FeedJpaEntity f3 = feedJpaRepository.save(TestEntityFactory.createFeed(user3, book, true, 50, 10, List.of()));

        // 피드 작성 순서 : user1이 작성한 f1 -> user2가 작성한 f2 -> user3가 작성한 f3
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
                .andExpect(jsonPath("$.data.recentWriters", hasSize(2)))
                // 정렬 조건 : 내가 팔로잉하는 사람들 중, 최근에 공개 피드를 작성한 사람 순
                .andExpect(jsonPath("$.data.recentWriters[0].userId", is(user2.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[0].nickname", is(user2.getNickname())))
                .andExpect(jsonPath("$.data.recentWriters[0].profileImageUrl", is(a0.getImageUrl())))
                .andExpect(jsonPath("$.data.recentWriters[1].userId", is(user1.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[1].nickname", is(user1.getNickname())))
                .andExpect(jsonPath("$.data.recentWriters[1].profileImageUrl", is(a0.getImageUrl())));
    }

    @Test
    @DisplayName("비공개 피드를 작성한 사람들은 조회되지 않습니다.")
    void show_my_following_recent_writers_private_feed_writer_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity user1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "user1"));

        // me가 user1를 팔로잉하였음
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, user1));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());
        // user1이 작성한 비공개 피드
        FeedJpaEntity f1 = feedJpaRepository.save(TestEntityFactory.createFeed(user1, book, false, 50, 10, List.of()));

        //when //then
        mockMvc.perform(get("/users/my-followings/recent-feeds")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentWriters", hasSize(0)));       // 아무도 조회되지 않음
    }

    @Test
    @DisplayName("내가 팔로잉 하는 사람들 중 최근에 공개 피드를 작성한 사람들이 많을 경우, 최대 10명만 반환됩니다.")
    void show_my_following_recent_writers_too_many_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
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
                .andExpect(jsonPath("$.data.recentWriters", hasSize(10)))
                // 정렬 조건 : 내가 팔로잉하는 사람들 중, 최근에 공개 피드를 작성한 사람 순
                .andExpect(jsonPath("$.data.recentWriters[0].userId", is(user12.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[1].userId", is(user11.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[2].userId", is(user10.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[3].userId", is(user9.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[4].userId", is(user8.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[5].userId", is(user7.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[6].userId", is(user6.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[7].userId", is(user5.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[8].userId", is(user4.getUserId().intValue())))
                .andExpect(jsonPath("$.data.recentWriters[9].userId", is(user3.getUserId().intValue())));
    }
}
