package konkuk.thip.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.FollowingJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 팔로워 조회 API 통합 테스트")
class UserGetFollowersApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private FollowingJpaRepository followingJpaRepository;

    private UserJpaEntity targetUser; // 팔로워를 조회할 대상 사용자
    private List<UserJpaEntity> followerUsers; // 팔로워 12명

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

        // 대상 사용자
        targetUser = userJpaRepository.save(TestEntityFactory.createUser(alias));

        // 팔로워 12명 생성 및 저장
        followerUsers = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            UserJpaEntity follower = userJpaRepository.save(TestEntityFactory.createUser(alias));
            followerUsers.add(follower);
            followingJpaRepository.save(TestEntityFactory.createFollowing(follower, targetUser));
        }
    }

    @AfterEach
    void tearDown() {
        followingJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("팔로워가 12명일 때 2페이지에 걸쳐 모두 조회되고 커서가 올바르게 작동한다.")
    void getFollowersWithCursorPaging() throws Exception {
        // 1. 첫 번째 요청 (cursor 없음)
        ResultActions firstPageResult = mockMvc.perform(
                get("/users/{userId}/followers", targetUser.getUserId())
        );

        firstPageResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerList", hasSize(10)))
                .andExpect(jsonPath("$.data.isFirst").value(true))
                .andExpect(jsonPath("$.data.isLast").value(false))
                .andExpect(jsonPath("$.data.nextCursor").exists());

        // 커서 추출
        String responseBody = firstPageResult.andReturn().getResponse().getContentAsString();
        String nextCursor = objectMapper.readTree(responseBody)
                .path("data")
                .path("nextCursor")
                .asText();

        // 2. 두 번째 요청 (cursor 사용)
        ResultActions secondPageResult = mockMvc.perform(
                get("/users/{userId}/followers", targetUser.getUserId())
                        .param("cursor", nextCursor)
        );

        secondPageResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followerList", hasSize(2)))
                .andExpect(jsonPath("$.data.isFirst").value(false))
                .andExpect(jsonPath("$.data.isLast").value(true))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }
}