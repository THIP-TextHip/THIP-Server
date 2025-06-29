package konkuk.thip.book.adapter.in.web;

import jakarta.validation.constraints.Pattern;
import konkuk.thip.book.adapter.in.web.response.GetBookDetailSearchResponse;
import konkuk.thip.book.adapter.in.web.response.GetBookSearchListResponse;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@Slf4j
public class BookQueryController {

    private final BookSearchUseCase bookSearchUseCase;

    //책 검색결과 조회
    @GetMapping("/books")
    public BaseResponse<GetBookSearchListResponse> getBookSearchList(@RequestParam final String keyword,
                                                                     @RequestParam final int page) {
        return BaseResponse.ok(GetBookSearchListResponse.of(bookSearchUseCase.searchBooks(keyword, page), page));
    }

    //책 상세검색 결과 조회
    @GetMapping("/books/{isbn}")
    public BaseResponse<GetBookDetailSearchResponse> getBookDetailSearch(@PathVariable("isbn")
                                                                             @Pattern(regexp = "\\d{13}") final String isbn,
                                                                         @UserId final Long userId) {



        return BaseResponse.ok(GetBookDetailSearchResponse.of(bookSearchUseCase.searchDetailBooks(isbn,userId)));
    }

}
