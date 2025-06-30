package konkuk.thip.book.adapter.in.web;

import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;
import konkuk.thip.recentSearch.adapter.out.persistence.RecentSearchJpaRepository;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserRole;
import konkuk.thip.user.adapter.out.persistence.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private RecentSearchJpaRepository recentSearchJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String testToken;

    @BeforeEach
    void setUp() {

        AliasJpaEntity alias = aliasJpaRepository.save(AliasJpaEntity.builder()
                .value("책벌레")
                .color("blue")
                .imageUrl("http://image.url")
                .build());

        UserJpaEntity user = userJpaRepository.save(UserJpaEntity.builder()
                .oauth2Id("kakao_432708231")
                .nickname("User1")
                .imageUrl("https://avatar1.jpg")
                .role(UserRole.USER)
                .aliasForUserJpaEntity(alias)
                .build());

        RecentSearchJpaEntity recentSearch = recentSearchJpaRepository.save(RecentSearchJpaEntity.builder()
                .searchTerm("테스트검색어")
                .type(SearchType.BOOK_SEARCH)
                .userJpaEntity(user)
                .build());

        testToken = jwtUtil.createAccessToken(user.getUserId());
    }

    @AfterEach
    void tearDown() {
        recentSearchJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        aliasJpaRepository.deleteAll();
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
        assertThat(recentSearch.getType()).isEqualTo(SearchType.BOOK_SEARCH);
        assertThat(recentSearch.getUserJpaEntity().getUserId()).isEqualTo(user.getUserId());
    }
}
