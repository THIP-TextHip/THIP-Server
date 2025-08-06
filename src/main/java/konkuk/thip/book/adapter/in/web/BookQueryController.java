package konkuk.thip.book.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import konkuk.thip.book.adapter.in.web.response.BookDetailSearchResponse;
import konkuk.thip.book.adapter.in.web.response.BookMostSearchResponse;
import konkuk.thip.book.adapter.in.web.response.BookRecruitingRoomsResponse;
import konkuk.thip.book.adapter.in.web.response.BookSearchListResponse;
import konkuk.thip.book.application.port.in.BookMostSearchUseCase;
import konkuk.thip.book.application.port.in.BookRecruitingRoomsUseCase;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Book Query API", description = "책 조회 관련 API")
@Validated
@RestController
@RequiredArgsConstructor
public class BookQueryController {

    private final BookSearchUseCase bookSearchUseCase;
    private final BookMostSearchUseCase bookMostSearchUseCase;
    private final BookRecruitingRoomsUseCase bookRecruitingRoomsUseCase;

    @Operation(
            summary = "책 검색결과 조회",
            description = "사용자가 입력한 키워드로 책을 검색합니다."
    )
    @ExceptionDescription(BOOK_SEARCH)
    @GetMapping("/books")
    public BaseResponse<BookSearchListResponse> showBookSearchList(
            @Parameter(description = "검색 키워드", example = "해리포터") @RequestParam final String keyword,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam final int page,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(BookSearchListResponse.of(bookSearchUseCase.searchBooks(keyword, page,userId), page));
    }

    //책 상세검색 결과 조회
    @Operation(
            summary = "책 상세검색 결과 조회",
            description = "ISBN을 통해 책의 상세 정보를 조회합니다."
    )
    @ExceptionDescription(BOOK_DETAIL_SEARCH)
    @GetMapping("/books/{isbn}")
    public BaseResponse<BookDetailSearchResponse> showBookDetailSearch(
            @Parameter(description = "책의 ISBN 번호 (13자리 숫자)", example = "9781234567890")
            @PathVariable("isbn") @Pattern(regexp = "\\d{13}") final String isbn,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(BookDetailSearchResponse.of(bookSearchUseCase.searchDetailBooks(isbn,userId)));
    }

    //가장 많이 검색된 책 조회
    @Operation(
            summary = "가장 많이 검색된 책 조회",
            description = "사용자가 가장 많이 검색한 책들을 조회합니다."
    )
    @ExceptionDescription(POPULAR_BOOK_SEARCH)
    @GetMapping("/books/most-searched")
    public BaseResponse<BookMostSearchResponse> showMostSearchedBooks(
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(BookMostSearchResponse.of(bookMostSearchUseCase.getMostSearchedBooks(userId)));
    }

    @Operation(
            summary = "특정 책으로 모집중인 방 조회",
            description = "책의 ISBN을 통해 해당 책과 관련된 모집중인 방들을 조회합니다."
    )
    @GetMapping("/books/{isbn}/recruiting-rooms")
    public BaseResponse<BookRecruitingRoomsResponse> showRecruitingRoomsWithBook(
            @Parameter(description = "책의 ISBN 번호 (13자리 숫자)", example = "9781234567890")
            @PathVariable("isbn") @Pattern(regexp = "\\d{13}") final String isbn,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(required = false) final String cursor
    ) {
        return BaseResponse.ok(bookRecruitingRoomsUseCase.getRecruitingRoomsWithBook(isbn, cursor));
    }

}
