package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookDetailSearchUseCase;
import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import konkuk.thip.book.application.port.out.SearchDetailBookQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookDetailSearchService implements BookDetailSearchUseCase {

    private final SearchDetailBookQueryPort searchDetailBookQueryPort;


//    public NaverBookParseResult searchBooks(String keyword, int page) {
//
//        if (keyword == null || keyword.isBlank()) {
//            throw new BusinessException(BOOK_KEYWORD_REQUIRED);
//        }
//
//        if (page < 1) {
//            throw new BusinessException(BOOK_PAGE_NUMBER_INVALID);
//        }
//
//        //유저의 최근검색어 로직 추가
//
//        int start = (page - 1) * PAGE_SIZE + 1; //검색 시작 위치
//        NaverBookParseResult result = searchBookQueryPort.findBooksByKeyword(keyword, start);
//
//        int totalElements = result.total();
//        int totalPages = (totalElements + PAGE_SIZE - 1) / PAGE_SIZE;
//        if ( totalElements!=0 && page > totalPages) {
//            throw new BusinessException(BOOK_SEARCH_PAGE_OUT_OF_RANGE);
//        }
//
//        return result;
//    }

    @Override
    public BookDetailSearchResult searchDetailBooks(String isbn) {

        NaverDetailBookParseResult naverDetailBookParseResult =
                searchDetailBookQueryPort.findDetailBookByKeyword(isbn);



        return BookDetailSearchResult.of(naverDetailBookParseResult,0,0,true);
    }
}