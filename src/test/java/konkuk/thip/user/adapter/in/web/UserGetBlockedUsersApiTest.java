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

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("[성공] 차단 목록은 size 만큼 끊어 커서로 이어진다. (무한스크롤)")
    void getBlockedUsers_paging() throws Exception {
        // given : 25명을 차단한다
        Alias alias = TestEntityFactory.createLiteratureAlias();
        for (int i = 0; i < 25; i++) {
            UserJpaEntity target = userJpaRepository.save(TestEntityFactory.createUser(alias, "blocked" + i));
            userBlockJpaRepository.save(TestEntityFactory.createUserBlock(user, target));
        }
        userBlockJpaRepository.flush();

        // when & then : 1페이지 10개, 총 개수는 첫 페이지에만 내려온다
        String cursor = readPage(null, 10, 25, false);

        // when & then : 2페이지 10개, 총 개수는 null
        cursor = readPage(cursor, 10, null, false);

        // when & then : 3페이지 5개, isLast = true, nextCursor 없음
        readPage(cursor, 5, null, true);
    }

    /**
     * 한 페이지를 조회해 크기·총개수·isLast 를 검증하고 다음 커서를 돌려준다.
     */
    private String readPage(String cursor, int expectedSize, Integer expectedTotal, boolean expectedLast) throws Exception {
        var request = get(BLOCKED_USERS_API_PATH).requestAttr("userId", user.getUserId()).param("size", "10");
        if (cursor != null) {
            request = request.param("cursor", cursor);
        }

        String body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedUsers", hasSize(expectedSize)))
                .andExpect(jsonPath("$.data.isLast").value(expectedLast))
                .andExpect(expectedTotal == null
                        ? jsonPath("$.data.totalBlockedUserCount").doesNotExist()
                        : jsonPath("$.data.totalBlockedUserCount").value(expectedTotal))
                .andReturn().getResponse().getContentAsString();

        String next = com.jayway.jsonpath.JsonPath.parse(body).read("$.data.nextCursor", String.class);
        if (expectedLast) {
            assertThat(next).isNull();
        } else {
            assertThat(next).isNotBlank();
        }
        return next;
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
