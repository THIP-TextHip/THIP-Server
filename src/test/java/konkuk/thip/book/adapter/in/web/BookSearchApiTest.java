package konkuk.thip.book.adapter.in.web;

import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchType;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
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

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("[통합] 방 검색 api 통합 테스트")
class BookSearchApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RecentSearchJpaRepository recentSearchJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String testToken;

    @BeforeEach
    void setUp() {
        Alias alias = TestEntityFactory.createLiteratureAlias();

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7))
                .role(UserRole.USER)
                .alias(alias)
                .build());

        RecentSearchJpaEntity recentSearch = recentSearchJpaRepository.save(RecentSearchJpaEntity.builder()
                .searchTerm("테스트검색어")
                .type(RecentSearchType.BOOK_SEARCH)
                .userJpaEntity(user)
                .build());

        testToken = jwtUtil.createAccessToken(user.getUserId());
    }

    @AfterEach
    void tearDown() {
        recentSearchJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("책 검색 API 정상 호출 - 키워드와 페이지 번호가 주어졌을 때")
    void searchBooks_success() throws Exception {

        // given
        String keyword = "테스트";
        int page = 1;

        // when & then
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + testToken)
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("isFinalized", String.valueOf(false))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.searchResult").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.first").value(true));
    }

    @Test
    @DisplayName("책 검색 API 실패 - 페이지가 범위를 벗어났을 때 400 에러 발생")
    void searchBooks_pageOutOfRange() throws Exception {

        // given
        String keyword = "테스트";
        int page = 99999; // totalPages보다 큰 값으로 가정

        // when & then
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + testToken)
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("isFinalized", String.valueOf(false))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_SEARCH_PAGE_OUT_OF_RANGE.getCode()))
                .andExpect(jsonPath("$.message", containsString("검색어 페이지가 범위를 벗어났습니다")));
    }


    @Test
    @DisplayName("책 검색 API 실패 - 키워드가 비어서 넘어올 때 400 에러 발생")
    void searchBooks_keywordMissing_badRequest() throws Exception {

        // given
        String keyword = "";
        int page = 1;

        // when & then
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + testToken)
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("isFinalized", String.valueOf(false))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_KEYWORD_REQUIRED.getCode()))
                .andExpect(jsonPath("$.message", containsString("검색어는 필수 입력값입니다")));
    }

    @Test
    @DisplayName("책 검색 API 실패 - 페이지 번호가 1 미만일 때 400 에러 발생")
    void searchBooks_pageInvalid_badRequest() throws Exception {

        // given
        String keyword = "테스트";
        int page = 0;

        // when & then
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + testToken)
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("isFinalized", String.valueOf(false))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_PAGE_NUMBER_INVALID.getCode()))
                .andExpect(jsonPath("$.message", containsString("페이지 번호는 1 이상의 값이어야 합니다")));
    }


    @Test
    @DisplayName("책 검색 성공 시 최근검색어 저장")
    void searchBooks_savesRecentSearch() throws Exception {

        // given
        String keyword = "테스트";
        int page = 1;

        // when
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + testToken)
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("isFinalized", String.valueOf(true))     // 검색 완료일 경우 : 최근검색어로 저장된다
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        // 최근검색어가 저장되었는지 확인
        UserJpaEntity user = userJpaRepository.findAll().get(0);
        RecentSearchJpaEntity recentSearch = recentSearchJpaRepository.findAll().stream()
                .filter(rs -> rs.getUserJpaEntity().getUserId().equals(user.getUserId()))
                .filter(rs -> rs.getSearchTerm().equals(keyword))
                .findFirst()
                .orElse(null);

        assertThat(recentSearch).isNotNull();
        assertThat(recentSearch.getSearchTerm()).isEqualTo(keyword);
        assertThat(recentSearch.getType()).isEqualTo(RecentSearchType.BOOK_SEARCH);
        assertThat(recentSearch.getUserJpaEntity().getUserId()).isEqualTo(user.getUserId());
    }
}
