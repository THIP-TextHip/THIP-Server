package konkuk.thip.recentSearch.adapter.in.web;

import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[통합] 최근 검색어 삭제 API 테스트")
class RecentSearchDeleteApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private RecentSearchJpaRepository recentSearchJpaRepository;

    private Long currentUserId;
    private Long otherUserId;
    private Long recentSearchId;

    @BeforeEach
    void setUp() {
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createLiteratureAlias());

        // 요청 사용자
        UserJpaEntity currentUser = userJpaRepository.save(TestEntityFactory.createUser(alias, "요청자"));
        currentUserId = currentUser.getUserId();

        // 다른 사용자
        UserJpaEntity otherUser = userJpaRepository.save(TestEntityFactory.createUser(alias, "다른유저"));
        otherUserId = otherUser.getUserId();

        // currentUser가 추가한 최근 검색어
        RecentSearchJpaEntity entity = recentSearchJpaRepository.save(
                RecentSearchJpaEntity.builder()
                        .searchTerm("삭제테스트")
                        .type(SearchType.USER_SEARCH)
                        .userJpaEntity(currentUser)
                        .build()
        );
        recentSearchId = entity.getRecentSearchId();
    }

    @Test
    @DisplayName("성공적으로 최근 검색어를 삭제한다")
    void deleteRecentSearch_success() throws Exception {
        // when
        mockMvc.perform(delete("/recent-searches/{recentSearchId}", recentSearchId)
                        .requestAttr("userId", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        List<RecentSearchJpaEntity> list = recentSearchJpaRepository.findAll();
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자가 추가한 최근 검색어는 삭제할 수 없다")
    void deleteRecentSearch_fail_notOwner() throws Exception {
        mockMvc.perform(delete("/recent-searches/{recentSearchId}", recentSearchId)
                        .requestAttr("userId", otherUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.RECENT_SEARCH_NOT_ADDED_BY_USER.getCode()));

        // DB에 여전히 남아있음
        assertThat(recentSearchJpaRepository.findById(recentSearchId)).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 최근 검색어를 삭제하려 하면 예외가 발생한다")
    void deleteRecentSearch_fail_notFound() throws Exception {
        Long notExistingId = 9999L;

        mockMvc.perform(delete("/recent-searches/{recentSearchId}", notExistingId)
                        .requestAttr("userId", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // 기존 데이터는 그대로
        assertThat(recentSearchJpaRepository.findAll()).hasSize(1);
    }
}