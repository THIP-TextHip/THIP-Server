package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookRedisCommandPort;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookMostSearchRankService {

    private final BookApiQueryPort bookApiQueryPort;
    private final BookRedisQueryPort bookRedisQueryPort;
    private final BookRedisCommandPort bookRedisCommandPort;
    private final BookCommandPort bookCommandPort;

    // 매일 0시 실행
    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public void updateDailySearchRank() {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 전날 검색 카운트 Top 5 조회
        List<Map.Entry<String, Double>> top5 = bookRedisQueryPort.getBookSearchCountTopN(yesterday, 5);

        // 기존 랭킹 책 상제정보 키 삭제
        bookRedisCommandPort.deleteBookSearchRankDetail(yesterday);
        // 기존 랭킹 키 삭제
        bookRedisCommandPort.deleteBookSearchRank(yesterday);
        // 전날 카운트 키 삭제
        bookRedisCommandPort.deleteBookSearchCount(yesterday);

        // Top 5 저장
        if (!top5.isEmpty()) { //PM과 상의 후 결정
            List<String> isbns = top5.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            List<Double> scores = top5.stream().map(Map.Entry::getValue).collect(Collectors.toList());
            bookRedisCommandPort.saveBookSearchRank(isbns, scores, yesterday);

            // Top 5 상세정보 저장 추가
            List<BookMostSearchResult.BookRankInfo> bookRankDetails = new ArrayList<>();
            int rank = 1;
            for (String isbn : isbns) {
                try {
                    Book book = bookCommandPort.findByIsbn(isbn);
                    bookRankDetails.add(BookMostSearchResult.BookRankInfo.builder()
                            .rank(rank++)
                            .title(book.getTitle())
                            .imageUrl(book.getImageUrl())
                            .isbn(isbn)
                            .build());
                } catch (EntityNotFoundException e) {
                    // DB에 없으면 Naver API에서 상세 정보 조회
                    NaverDetailBookParseResult naverResult = bookApiQueryPort.findDetailBookByIsbn(isbn);
                    bookRankDetails.add(BookMostSearchResult.BookRankInfo.builder()
                            .rank(rank++)
                            .title(naverResult.title())
                            .imageUrl(naverResult.imageUrl())
                            .isbn(isbn)
                            .build());
                }
            }
            bookRedisCommandPort.saveBookSearchRankDetail(bookRankDetails, yesterday);
        }
    }
}
