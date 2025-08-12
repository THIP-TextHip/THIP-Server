package konkuk.thip.feed.adapter.in.web;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 피드 화면에서의 유저 정보 조회 api 통합 테스트")
class FeedShowUserInfoApiTest {

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
    @DisplayName("내 피드에서의 유저 정보를 조회할 경우, 내 개인 정보, 나의 팔로워 정보, 내가 작성한 모든 피드 개수 를 반환한다.")
    void feed_show_mine_info_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        AliasJpaEntity a1 = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity follower1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower1"));
        UserJpaEntity follower2 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower2"));
        FollowingJpaEntity followingJpaEntity1 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower1, me));// follower1 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity2 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower2, me));// follower2 가 me를 follow 하는 상황

        // 팔로우 한 시각을 조정 (follower1 -> follower2 순 으로 팔로잉을 했다고 가정)
        followingJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), followingJpaEntity1.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), followingJpaEntity2.getFollowingId());
        jdbcTemplate.update(
                "UPDATE users SET follower_count = ? WHERE user_id = ?",
                2, me.getUserId());      // me 의 followerCount 값을 2로 update

        // 피드 생성 및 생성일 직접 설정
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book
        feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true));        // 공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(me, book, true));        // 공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(me, book, false));       // 비공개글

        //when //then
        mockMvc.perform(get("/feeds/mine/info")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileImageUrl", is(a0.getImageUrl())))
                .andExpect(jsonPath("$.data.nickname", is(me.getNickname())))
                .andExpect(jsonPath("$.data.aliasName", is(a0.getValue())))
                .andExpect(jsonPath("$.data.aliasColor", is(a0.getColor())))
                .andExpect(jsonPath("$.data.followerCount", is(2)))
                .andExpect(jsonPath("$.data.totalFeedCount", is(3)))
                // 팔로워 유저의 프로필 이미지 정렬 순서 : 팔로잉을 최신에 맺은 순 (follower2 -> follower1 순)
                .andExpect(jsonPath("$.data.latestFollowerProfileImageUrls", is(List.of(a1.getImageUrl(), a0.getImageUrl()))));
    }

    @Test
    @DisplayName("나를 팔로우하는 사람이 많을 경우, 팔로우 맺은 일자 기준 최신순으로 최대 5명의 팔로워 프로필 이미지만을 반환한다.")
    void feed_show_mine_info_follower_many_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        AliasJpaEntity a1 = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity follower1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower1"));
        UserJpaEntity follower2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower2"));
        UserJpaEntity follower3 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower3"));
        UserJpaEntity follower4 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower4"));
        UserJpaEntity follower5 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower5"));
        UserJpaEntity follower6 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower6"));
        UserJpaEntity follower7 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower7"));
        FollowingJpaEntity followingJpaEntity1 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower1, me));// follower1 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity2 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower2, me));// follower2 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity3 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower3, me));// follower3 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity4 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower4, me));// follower4 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity5 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower5, me));// follower5 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity6 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower6, me));// follower6 가 me를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity7 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower7, me));// follower7 가 me를 follow 하는 상황

        // 팔로우 한 시각을 조정 (follower1 -> follower2 -> ,,, -> follower7 순 으로 팔로잉을 했다고 가정)
        followingJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), followingJpaEntity1.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), followingJpaEntity2.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), followingJpaEntity3.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), followingJpaEntity4.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), followingJpaEntity5.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), followingJpaEntity6.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), followingJpaEntity7.getFollowingId());
        jdbcTemplate.update(
                "UPDATE users SET follower_count = ? WHERE user_id = ?",
                7, me.getUserId());      // me 의 followerCount 값을 7로 update

        //when //then
        mockMvc.perform(get("/feeds/mine/info")
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerCount", is(7)))
                // 팔로워 유저의 프로필 이미지 정렬 순서 : 팔로잉을 최신에 맺은 순 (follower7 -> follower6 -> ,,, -> follower3 순)
                .andExpect(jsonPath("$.data.latestFollowerProfileImageUrls", is(List.of(
                        a0.getImageUrl(),
                        a1.getImageUrl(),
                        a1.getImageUrl(),
                        a0.getImageUrl(),
                        a1.getImageUrl()))));
    }

    @Test
    @DisplayName("특정 유저 피드에서의 유저 정보를 조회할 경우, 유저 개인 정보, 유저의 팔로워 정보, 유저가 작성한 모든 '공개' 피드 개수 를 반환한다.")
    void feed_show_user_info_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        AliasJpaEntity a1 = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity anotherUser = userJpaRepository.save(TestEntityFactory.createUser(a0, "anotherUser"));
        UserJpaEntity follower1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower1"));
        UserJpaEntity follower2 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower2"));
        FollowingJpaEntity followingJpaEntity1 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower1, anotherUser));// follower1 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity2 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower2, anotherUser));// follower2 가 anotherUser를 follow 하는 상황

        // 팔로우 한 시각을 조정 (follower1 -> follower2 순 으로 팔로잉을 했다고 가정)
        followingJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), followingJpaEntity1.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), followingJpaEntity2.getFollowingId());
        jdbcTemplate.update(
                "UPDATE users SET follower_count = ? WHERE user_id = ?",
                2, anotherUser.getUserId());      // anotherUser 의 followerCount 값을 2로 update

        // 피드 생성 및 생성일 직접 설정
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, true));        // 공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, true));        // 공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, false));       // 비공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, false));       // 비공개글

        //when //then
        mockMvc.perform(get("/feeds/users/{userId}/info", anotherUser.getUserId())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.creatorId", is(anotherUser.getUserId().intValue())))
                .andExpect(jsonPath("$.data.profileImageUrl", is(a0.getImageUrl())))
                .andExpect(jsonPath("$.data.nickname", is(anotherUser.getNickname())))
                .andExpect(jsonPath("$.data.aliasName", is(a0.getValue())))
                .andExpect(jsonPath("$.data.aliasColor", is(a0.getColor())))
                .andExpect(jsonPath("$.data.followerCount", is(2)))
                .andExpect(jsonPath("$.data.totalFeedCount", is(2)))        // 공개 글만
                .andExpect(jsonPath("$.data.isFollowing", is(false)))       // me는 anotherUser를 팔로잉하지 않음
                // 팔로워 유저의 프로필 이미지 정렬 순서 : 팔로잉을 최신에 맺은 순 (follower2 -> follower1 순)
                .andExpect(jsonPath("$.data.latestFollowerProfileImageUrls", is(List.of(a1.getImageUrl(), a0.getImageUrl()))));
    }

    @Test
    @DisplayName("accessToken의 유저가 특정 유저를 팔로잉하는 경우, isFollowing은 true이다.")
    void feed_show_user_info_isFollowing_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        AliasJpaEntity a1 = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity anotherUser = userJpaRepository.save(TestEntityFactory.createUser(a0, "anotherUser"));
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a1, "me"));
        followingJpaRepository.save(TestEntityFactory.createFollowing(me, anotherUser));// me 가 anotherUser를 follow 하는 상황

        // 피드 생성 및 생성일 직접 설정
        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook());        // 공통 Book
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, true));        // 공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, true));        // 공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, false));       // 비공개글
        feedJpaRepository.save(TestEntityFactory.createFeed(anotherUser, book, false));       // 비공개글

        //when //then
        mockMvc.perform(get("/feeds/users/{userId}/info", anotherUser.getUserId())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFollowing", is(true)))       // me는 anotherUser를 팔로잉함
                // 팔로워 유저의 프로필 이미지 정렬 순서 : 팔로잉을 최신에 맺은 순
                .andExpect(jsonPath("$.data.latestFollowerProfileImageUrls", is(List.of(a1.getImageUrl()))));
    }

    @Test
    @DisplayName("특정 유저를 팔로우하는 사람이 많을 경우, 팔로우 맺은 일자 기준 최신순으로 최대 5명의 팔로워 프로필 이미지만을 반환한다.")
    void feed_show_user_info_follower_many_test() throws Exception {
        //given
        AliasJpaEntity a0 = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        AliasJpaEntity a1 = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(a0, "me"));
        UserJpaEntity anotherUser = userJpaRepository.save(TestEntityFactory.createUser(a0, "anotherUser"));
        UserJpaEntity follower1 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower1"));
        UserJpaEntity follower2 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower2"));
        UserJpaEntity follower3 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower3"));
        UserJpaEntity follower4 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower4"));
        UserJpaEntity follower5 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower5"));
        UserJpaEntity follower6 = userJpaRepository.save(TestEntityFactory.createUser(a1, "follower6"));
        UserJpaEntity follower7 = userJpaRepository.save(TestEntityFactory.createUser(a0, "follower7"));
        FollowingJpaEntity followingJpaEntity1 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower1, anotherUser));// follower1 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity2 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower2, anotherUser));// follower2 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity3 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower3, anotherUser));// follower3 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity4 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower4, anotherUser));// follower4 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity5 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower5, anotherUser));// follower5 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity6 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower6, anotherUser));// follower6 가 anotherUser를 follow 하는 상황
        FollowingJpaEntity followingJpaEntity7 = followingJpaRepository.save(TestEntityFactory.createFollowing(follower7, anotherUser));// follower7 가 anotherUser를 follow 하는 상황

        // 팔로우 한 시각을 조정 (follower1 -> follower2 -> ,,, -> follower7 순 으로 팔로잉을 했다고 가정)
        followingJpaRepository.flush();
        LocalDateTime base = LocalDateTime.now();
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(30)), followingJpaEntity1.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(25)), followingJpaEntity2.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(20)), followingJpaEntity3.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(15)), followingJpaEntity4.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(10)), followingJpaEntity5.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(5)), followingJpaEntity6.getFollowingId());
        jdbcTemplate.update(
                "UPDATE followings SET created_at = ? WHERE following_id = ?",
                Timestamp.valueOf(base.minusMinutes(1)), followingJpaEntity7.getFollowingId());
        jdbcTemplate.update(
                "UPDATE users SET follower_count = ? WHERE user_id = ?",
                7, anotherUser.getUserId());      // anotherUser 의 followerCount 값을 7로 update

        //when //then
        mockMvc.perform(get("/feeds/users/{userId}/info", anotherUser.getUserId())
                        .requestAttr("userId", me.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerCount", is(7)))
                // 팔로워 유저의 프로필 이미지 정렬 순서 : 팔로잉을 최신에 맺은 순 (follower7 -> follower6 -> ,,, -> follower3 순)
                .andExpect(jsonPath("$.data.latestFollowerProfileImageUrls", is(List.of(
                        a0.getImageUrl(),
                        a1.getImageUrl(),
                        a1.getImageUrl(),
                        a0.getImageUrl(),
                        a1.getImageUrl()))));
    }
}
