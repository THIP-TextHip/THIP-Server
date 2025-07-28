package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.util.TestEntityFactory;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 팔로잉 여부 조회 API 통합 테스트")
class UserIsFollowingApiTest {

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
    @DisplayName("팔로우 관계가 존재하면 true를 반환한다.")
    void isFollowing_true() throws Exception {
        // given
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias));

        UserJpaEntity target = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // 팔로잉 관계 저장
        followingJpaRepository.save(FollowingJpaEntity.builder()
                .userJpaEntity(user)
                .followingUserJpaEntity(target)
                .build());

        // when & then
        mockMvc.perform(get("/users/{targetUserId}/is-following", target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFollowing").value(true));
    }

    @Test
    @DisplayName("팔로우 관계가 없으면 false를 반환한다.")
    void isFollowing_false() throws Exception {
        // given
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());

        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias));

        UserJpaEntity target = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // when & then
        mockMvc.perform(get("/users/{targetUserId}/is-following", target.getUserId())
                        .requestAttr("userId", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFollowing").value(false));
    }
}