package konkuk.thip.recentSearch.adapter.in.web;

import jakarta.persistence.EntityManager;
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

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@DisplayName("[통합] 최근 검색어 조회 API 테스트")
class RecentSearchGetApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RecentSearchJpaRepository recentSearchJpaRepository;

    private Long currentUserId;

    @BeforeEach
    void setUp() {
        // 사용자 및 별칭 생성
        Alias alias = TestEntityFactory.createLiteratureAlias();
        UserJpaEntity currentUser = userJpaRepository.save(TestEntityFactory.createUser(alias, "검색자"));
        currentUserId = currentUser.getUserId();

        // 최근 검색어 6개 저장 (6개 중 최신 5개만 조회될 예정)
        IntStream.rangeClosed(1, 6).forEach(i -> {
            RecentSearchJpaEntity saved = recentSearchJpaRepository.save(
                    RecentSearchJpaEntity.builder()
                            .searchTerm("검색어" + i)
                            .type(RecentSearchType.USER_SEARCH)
                            .userJpaEntity(currentUser)
                            .build()
            );

            // JPQL update로 modifiedAt을 강제로 원하는 값으로 덮기
            em.createQuery("update RecentSearchJpaEntity r set r.modifiedAt = :time where r.id = :id")
                    .setParameter("time", LocalDateTime.now().minusMinutes(i))
                    .setParameter("id", saved.getRecentSearchId())
                    .executeUpdate();
        });

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("최근 검색어를 최신순으로 최대 5개까지 조회한다")
    void getRecentSearches() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
                get("/recent-searches")
                        .param("type", "USER")
                        .requestAttr("userId", currentUserId)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentSearchList", hasSize(5)))
                .andExpect(jsonPath("$.data.recentSearchList[0].searchTerm").value("검색어1"))
                .andExpect(jsonPath("$.data.recentSearchList[1].searchTerm").value("검색어2"))
                .andExpect(jsonPath("$.data.recentSearchList[2].searchTerm").value("검색어3"))
                .andExpect(jsonPath("$.data.recentSearchList[3].searchTerm").value("검색어4"))
                .andExpect(jsonPath("$.data.recentSearchList[4].searchTerm").value("검색어5"));

        // DB에 저장된 최근 검색어 개수 검증
        assertEquals(6, recentSearchJpaRepository.findAll().size());
    }
}