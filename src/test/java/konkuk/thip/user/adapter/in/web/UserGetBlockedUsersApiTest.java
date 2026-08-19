package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.block.UserBlockJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 차단 목록 조회 api 통합 테스트")
class UserGetBlockedUsersApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private UserBlockJpaRepository userBlockJpaRepository;

    private static final String BLOCKED_USERS_API_PATH = "/users/blocks";

    private UserJpaEntity user;
    private UserJpaEntity blockedUser;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();
        user = userJpaRepository.save(TestEntityFactory.createUser(alias, "차단하는사람"));
        blockedUser = userJpaRepository.save(TestEntityFactory.createUser(alias, "차단당한사람"));
    }

    @AfterEach
    void tearDown() {
        userBlockJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[성공] 내가 차단한 사용자 목록을 조회한다.")
    void getBlockedUsers_success() throws Exception {
        // given
        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(user, blockedUser));

        // when & then
        mockMvc.perform(get(BLOCKED_USERS_API_PATH)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedUsers", hasSize(1)))
                .andExpect(jsonPath("$.data.blockedUsers[0].userId").value(blockedUser.getUserId()))
                .andExpect(jsonPath("$.data.blockedUsers[0].nickname").value("차단당한사람"))
                .andExpect(jsonPath("$.data.totalBlockedUserCount").value(1))
                .andExpect(jsonPath("$.data.isLast").value(true));
    }

    @Test
    @DisplayName("[성공] 차단한 사용자가 없으면 빈 목록을 반환한다.")
    void getBlockedUsers_empty() throws Exception {
        mockMvc.perform(get(BLOCKED_USERS_API_PATH)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedUsers", hasSize(0)))
                .andExpect(jsonPath("$.data.totalBlockedUserCount").value(0))
                .andExpect(jsonPath("$.data.isLast").value(true));
    }

    @Test
    @DisplayName("[성공] 나를 차단한 사용자는 내 차단 목록에 나타나지 않는다.")
    void getBlockedUsers_excludes_reverse_block() throws Exception {
        // given : 상대가 나를 차단
        userBlockJpaRepository.save(TestEntityFactory.createUserBlock(blockedUser, user));

        // when & then : 내 차단 목록은 비어 있다
        mockMvc.perform(get(BLOCKED_USERS_API_PATH)
                        .requestAttr("userId", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedUsers", hasSize(0)));
    }
}
