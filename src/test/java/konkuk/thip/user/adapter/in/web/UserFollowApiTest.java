package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import org.junit.jupiter.api.AfterEach;
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
@DisplayName("[통합] 팔로잉 상태 변경 API 통합 테스트")
class UserFollowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private FollowingJpaRepository followingJpaRepository;

    @AfterEach
    void tearDown() {
        followingJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("팔로우 요청 후 언팔로우 요청 시 엔티티가 삭제되었는지 확인한다.")
    void changeFollowingState_follow_then_unfollow() throws Exception {
        // 사용자 2명 저장
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

        UserJpaEntity followingUser = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("user100")
                .oauth2Id("oauth2_user100")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        UserJpaEntity target = userJpaRepository.save(UserJpaEntity.builder()
                .nickname("user200")
                .oauth2Id("oauth2_user200")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        // 팔로우 요청
        mockMvc.perform(post("/users/following/{followingUserId}", target.getUserId())
                        .requestAttr("userId", followingUser.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFollowing").value(true));

        // DB에 팔로우 상태가 ACTIVE로 저장되었는지 확인
        FollowingJpaEntity followEntity = followingJpaRepository.findByUserAndTargetUser(followingUser.getUserId(), target.getUserId()).orElseThrow();
        assertThat(followEntity.getStatus().name()).isEqualTo("ACTIVE");

        UserJpaEntity userJpaEntity = userJpaRepository.findById(target.getUserId()).orElseThrow();
        assertThat(userJpaEntity.getFollowerCount()).isEqualTo(1); // 팔로워 수 증가 확인

        // 언팔로우 요청
        mockMvc.perform(post("/users/following/{followingUserId}", target.getUserId())
                        .requestAttr("userId", followingUser.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFollowing").value(false));

        // DB에서 삭제되었는지 확인
        Optional<FollowingJpaEntity> followingJpaEntityOptional = followingJpaRepository.findByUserAndTargetUser(followingUser.getUserId(), target.getUserId());
        assertThat(followingJpaEntityOptional.isPresent()).isFalse();

        userJpaEntity = userJpaRepository.findById(target.getUserId()).orElseThrow();
        assertThat(userJpaEntity.getFollowerCount()).isEqualTo(0); // 팔로워 수 감소 확인
    }
}