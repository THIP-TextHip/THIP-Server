package konkuk.thip.book.adapter.in.web;

import konkuk.thip.book.adapter.in.web.response.GetBookSearchListResponse;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.common.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookQueryController {

    private final BookSearchUseCase bookSearchUseCase;

    @GetMapping("/books")
    public BaseResponse<GetBookSearchListResponse> getBookSearchList(@RequestParam final String keyword,
                                                                     @RequestParam final int page) {
        NaverBookParseResult result = bookSearchUseCase.searchBooks(keyword, page);
        return BaseResponse.ok(GetBookSearchListResponse.of(result, page));
    }

}
