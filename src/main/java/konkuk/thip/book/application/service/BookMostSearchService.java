package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.in.web.response.GetBookMostSearchResponse;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookMostSearchUseCase;
import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import konkuk.thip.book.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookMostSearchService implements BookMostSearchUseCase {

    private final BookQueryPort bookQueryPort;
    private final BookRedisQueryPort bookRedisQueryPort;
    private final BookApiQueryPort bookApiQueryPort;

    @Override
    public BookMostSearchResult getMostSearchedBooks(Long userId) {

        // 오늘 날짜 기준
        LocalDate today = LocalDate.now();

        // Redis에서 오늘 날짜 기준 랭킹 Top 5 조회
        List<Map.Entry<String, Double>> top5 = bookRedisQueryPort.getBookSearchRank(today, 5);

        if (top5 == null || top5.isEmpty()) {
            return BookMostSearchResult.of(Collections.emptyList()); //PM과 상의 후 결정
        }

        // isbn 리스트 추출
        List<String> isbnList = top5.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // DB에서 isbn으로 책 정보 조회
        List<Book> books = bookQueryPort.findByIsbnIn(isbnList);
        // isbn -> Book 매핑
        Map<String, Book> bookMap = books.stream()
                .collect(Collectors.toMap(Book::getIsbn, b -> b));

        List<GetBookMostSearchResponse.BookRankInfo> bookRankInfos = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Double> entry : top5) {
            String isbn = entry.getKey();
            Book book = bookMap.get(isbn);
            if (book == null) {
                // DB에 없으면 Naver API에서 상세 정보 조회
                NaverDetailBookParseResult naverResult = bookApiQueryPort.findDetailBookByKeyword(isbn);
                bookRankInfos.add(GetBookMostSearchResponse.BookRankInfo.builder()
                        .rank(rank++)
                        .title(naverResult.title())
                        .imageUrl(naverResult.imageUrl())
                        .isbn(isbn)
                        .build());
            } else {
                bookRankInfos.add(GetBookMostSearchResponse.BookRankInfo.builder()
                        .rank(rank++)
                        .title(book.getTitle())
                        .imageUrl(book.getImageUrl())
                        .isbn(isbn)
                        .build());
            }
        }

        return BookMostSearchResult.of(bookRankInfos);

    }
}