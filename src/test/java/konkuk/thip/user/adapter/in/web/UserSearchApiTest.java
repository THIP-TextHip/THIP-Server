package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.domain.value.RecentSearchType;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 사용자 검색 API + 최근 검색어 저장 테스트")
class UserSearchApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RecentSearchJpaRepository recentSearchJpaRepository;

    private Long currentUserId;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        // 검색 요청을 하는 사용자
        UserJpaEntity currentUser = userJpaRepository.save(TestEntityFactory.createUser(alias, "검색자"));
        currentUserId = currentUser.getUserId();

        // 검색 대상 사용자들
        List.of("thipalpha", "thipbeta", "123thip", "thipgamma", "otheruser")
                .forEach(nickname -> userJpaRepository.save(TestEntityFactory.createUser(alias, nickname)));
    }

    @Test
    @DisplayName("사용자 검색 시 검색 결과 반환 + 최근 검색어 저장")
    void searchUsersAndSaveRecentSearch() throws Exception {
        String keyword = "thip";

        // when: 사용자 검색 API 호출
        ResultActions result = mockMvc.perform(
                get("/users")
                        .param("keyword", keyword)
                        .requestAttr("userId", currentUserId)
                        .param("size", "10")
                        .param("isFinalized", String.valueOf(true))     // 검색 완료 -> 최근 검색어 저장
        );

        // then: 검색 결과 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userList", hasSize(4)))
                .andExpect(jsonPath("$.data.userList[0].nickname").value("thipalpha"))
                .andExpect(jsonPath("$.data.userList[1].nickname").value("thipbeta"))
                .andExpect(jsonPath("$.data.userList[2].nickname").value("thipgamma"))
                .andExpect(jsonPath("$.data.userList[3].nickname").value("123thip"));

        // 최근 검색어 DB 저장 여부 검증
        List<RecentSearchJpaEntity> recentSearches = recentSearchJpaRepository.findAll();
        assertEquals(1, recentSearches.size());
        RecentSearchJpaEntity saved = recentSearches.get(0);
        assertEquals(keyword, saved.getSearchTerm());
        assertEquals(RecentSearchType.USER_SEARCH, saved.getType());
        assertEquals(currentUserId, saved.getUserJpaEntity().getUserId());
    }
}
