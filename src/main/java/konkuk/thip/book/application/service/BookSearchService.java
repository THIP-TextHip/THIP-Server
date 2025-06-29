package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.application.port.in.BookSearchUseCase;
import konkuk.thip.book.application.port.out.SearchBookQueryPort;
import konkuk.thip.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static konkuk.thip.book.adapter.out.api.NaverApiUtil.PAGE_SIZE;
import static konkuk.thip.common.exception.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BookSearchService implements BookSearchUseCase {

    private final SearchBookQueryPort searchBookQueryPort;

    @Override
    public NaverBookParseResult searchBooks(String keyword, int page) {

        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(BOOK_KEYWORD_REQUIRED);
        }

        if (page < 1) {
            throw new BusinessException(BOOK_PAGE_NUMBER_INVALID);
        }

        //유저의 최근검색어 로직 추가

        int start = (page - 1) * PAGE_SIZE + 1; //검색 시작 위치
        NaverBookParseResult result = searchBookQueryPort.findBooksByKeyword(keyword, start);

        int totalElements = result.total();
        int totalPages = (totalElements + PAGE_SIZE - 1) / PAGE_SIZE;
        if ( totalElements!=0 && page > totalPages) {
            throw new BusinessException(BOOK_SEARCH_PAGE_OUT_OF_RANGE);
        }

        return result;
    }

}