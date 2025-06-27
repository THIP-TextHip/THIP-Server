package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.in.web.response.BookDto;
import konkuk.thip.book.adapter.in.web.response.GetBookSearchListResponse;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.book.application.port.out.SearchBookQueryPort;
import konkuk.thip.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BookSearchService implements BookSearchUseCase {

    private static final int PAGE_SIZE = 10;
    private final SearchBookQueryPort SearchBookQueryPort;


    @Override
    public GetBookSearchListResponse searchBooks(String keyword, int page) {

        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(BOOK_KEYWORD_REQUIRED);
        }

        if (page < 1) {
            throw new BusinessException(BOOK_PAGE_NUMBER_INVALID);
        }

        //유저의 최근검색어 로직 추가

        int start = (page - 1) * PAGE_SIZE + 1; //검색 시작 위치
        NaverBookParseResult result = SearchBookQueryPort.findBooksByKeyword(keyword, start);

        int totalElements = result.total();
        int totalPages = (totalElements + PAGE_SIZE - 1) / PAGE_SIZE;
        if ( totalElements!=0 && page > totalPages) {
            throw new BusinessException(BOOK_SEARCH_PAGE_OUT_OF_RANGE);
        }
        boolean last = (page >= totalPages);
        boolean first = (page == 1);

        List<BookDto> bookDtos = result.books().stream()
                .map(book -> new BookDto(
                        book.getTitle(),
                        book.getImageUrl(),
                        book.getAuthorName(),
                        book.getPublisher(),
                        book.getIsbn()
                ))
                .toList();

        return new GetBookSearchListResponse(
                bookDtos,
                page,
                totalElements,
                totalPages,
                last,
                first
        );
    }

}