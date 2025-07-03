package konkuk.thip.book.adapter.in.web;

import konkuk.thip.common.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BookQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("책 검색 API 정상 호출 - 키워드와 페이지 번호가 주어졌을 때")
    void searchBooks_success() throws Exception {
        mockMvc.perform(get("/books")
                        .param("keyword", "테스트")
                        .param("page", "1")
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
        mockMvc.perform(get("/books")
                        .param("keyword", "테스트")
                        .param("page", "99999") // totalPages보다 큰 값으로 가정
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_SEARCH_PAGE_OUT_OF_RANGE.getCode()))
                .andExpect(jsonPath("$.message", containsString("검색어 페이지가 범위를 벗어났습니다")));
    }


    @Test
    @DisplayName("책 검색 API 실패 - 키워드가 비어서 넘어올 때 400 에러 발생")
    void searchBooks_keywordMissing_badRequest() throws Exception {
        mockMvc.perform(get("/books")
                        .param("page", "1")
                        .param("keyword", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_KEYWORD_REQUIRED.getCode()))
                .andExpect(jsonPath("$.message", containsString("검색어는 필수 입력값입니다")));
    }

    @Test
    @DisplayName("책 검색 API 실패 - 페이지 번호가 1 미만일 때 400 에러 발생")
    void searchBooks_pageInvalid_badRequest() throws Exception {
        mockMvc.perform(get("/books")
                        .param("keyword", "테스트")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BOOK_PAGE_NUMBER_INVALID.getCode()))
                .andExpect(jsonPath("$.message", containsString("페이지 번호는 1 이상의 값이어야 합니다")));
    }
}
