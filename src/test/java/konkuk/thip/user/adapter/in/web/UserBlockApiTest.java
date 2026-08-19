package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.UserBlockJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.block.UserBlockJpaRepository;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 사용자 차단 상태 변경 api 통합 테스트")
class UserBlockApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private UserBlockJpaRepository userBlockJpaRepository;
    @Autowired private FollowingJpaRepository followingJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;

    private static final String BLOCK_API_PATH = "/users/block/{targetUserId}";

    private UserJpaEntity user;
    private UserJpaEntity target;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias, "차단하는사람"));
        target = userJpaRepository.save(TestEntityFactory.createUser(alias, "차단당하는사람"));
    }

    @AfterEach
    void tearDown() {
        // 팔로우 API 호출 시 알림이 생성되므로 users 보다 먼저 정리해야 FK 제약에 걸리지 않는다
        notificationJpaRepository.deleteAllInBatch();
        userBlockJpaRepository.deleteAllInBatch();
        followingJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[성공] 차단 요청 후 차단 해제 요청 시 차단 관계가 생성되었다가 삭제된다.")
    void changeBlockState_block_then_unblock() throws Exception {
        // when : 차단 요청
        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBlocked").value(true));

        // then : 차단 관계가 저장된다
        UserBlockJpaEntity blockEntity = userBlockJpaRepository
                .findByUserAndBlockedUser(user.getUserId(), target.getUserId()).orElseThrow();
        assertThat(blockEntity.getStatus().name()).isEqualTo("ACTIVE");

        // when : 차단 해제 요청
        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBlocked").value(false));

        // then : 차단 관계가 삭제된다 (hard delete)
        Optional<UserBlockJpaEntity> deleted = userBlockJpaRepository
                .findByUserAndBlockedUser(user.getUserId(), target.getUserId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("[성공] 차단하면 양쪽 팔로우 관계가 해제되고 팔로워 수가 감소한다.")
    void changeBlockState_block_unfollows_both_ways() throws Exception {
        // given : 팔로우 API 로 서로 팔로우한 상태를 만든다 (followerCount 까지 정확히 반영하기 위해)
        follow(user.getUserId(), target.getUserId());
        follow(target.getUserId(), user.getUserId());

        assertThat(userJpaRepository.findById(user.getUserId()).orElseThrow().getFollowerCount()).isEqualTo(1);
        assertThat(userJpaRepository.findById(target.getUserId()).orElseThrow().getFollowerCount()).isEqualTo(1);

        // when : 차단
        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBlocked").value(true));

        // then : 양방향 팔로우가 모두 삭제된다
        assertThat(followingJpaRepository.findByUserAndTargetUser(user.getUserId(), target.getUserId())).isEmpty();
        assertThat(followingJpaRepository.findByUserAndTargetUser(target.getUserId(), user.getUserId())).isEmpty();

        // then : 양쪽 팔로워 수가 감소한다
        assertThat(userJpaRepository.findById(user.getUserId()).orElseThrow().getFollowerCount()).isZero();
        assertThat(userJpaRepository.findById(target.getUserId()).orElseThrow().getFollowerCount()).isZero();
    }

    @Test
    @DisplayName("[성공] 차단을 해제해도 팔로우 관계는 복구되지 않는다.")
    void changeBlockState_unblock_does_not_restore_follow() throws Exception {
        // given : 팔로우 후 차단
        follow(user.getUserId(), target.getUserId());

        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isOk());

        // when : 차단 해제
        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": false}"))
                .andExpect(status().isOk());

        // then : 팔로우는 복구되지 않는다
        assertThat(followingJpaRepository.findByUserAndTargetUser(user.getUserId(), target.getUserId())).isEmpty();
    }

    @Test
    @DisplayName("[400 에러 발생] 이미 차단한 사용자를 다시 차단할 수 없다.")
    void changeBlockState_already_blocked_fail() throws Exception {
        // given
        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(user, target));

        // when & then
        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_BLOCKED.getCode()));
    }

    @Test
    @DisplayName("[400 에러 발생] 차단하지 않은 사용자를 차단 해제할 수 없다.")
    void changeBlockState_already_unblocked_fail() throws Exception {
        mockMvc.perform(post(BLOCK_API_PATH, target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_UNBLOCKED.getCode()));
    }

    @Test
    @DisplayName("[400 에러 발생] 자기 자신은 차단할 수 없다.")
    void changeBlockState_self_block_fail() throws Exception {
        mockMvc.perform(post(BLOCK_API_PATH, user.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_CANNOT_BLOCK_SELF.getCode()));
    }

    @Test
    @DisplayName("[400 에러 발생] 차단 관계인 사용자는 팔로우할 수 없다.")
    void follow_blocked_user_fail() throws Exception {
        // given : 상대가 나를 차단한 상태
        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(target, user));

        // when & then
        mockMvc.perform(post("/users/following/{followingUserId}", target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_BLOCKED_CANNOT_INTERACT.getCode()));
    }

    private void follow(Long followerUserId, Long targetUserId) throws Exception {
        mockMvc.perform(post("/users/following/{followingUserId}", targetUserId)
                        .requestAttr("userId", followerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isOk());
    }
}
