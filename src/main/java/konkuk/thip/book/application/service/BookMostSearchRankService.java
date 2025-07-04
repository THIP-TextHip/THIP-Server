package konkuk.thip.book.application.service;

import konkuk.thip.book.application.port.out.BookRedisCommandPort;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookMostSearchRankService {

    private final BookRedisQueryPort bookRedisQueryPort;
    private final BookRedisCommandPort bookRedisCommandPort;

    // 매일 0시 실행
    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public void updateDailySearchRank() {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 전날 검색 카운트 Top 5 조회
        List<Map.Entry<String, Double>> top5 = bookRedisQueryPort.getBookSearchCountTopN(yesterday, 5);

        // 기존 랭킹 키 삭제
        bookRedisCommandPort.deleteBookSearchRank(yesterday);
        // 전날 카운트 키 삭제
        bookRedisCommandPort.deleteBookSearchCount(yesterday);

        // Top 5 저장
        if (!top5.isEmpty()) {
            List<String> isbns = top5.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            List<Double> scores = top5.stream().map(Map.Entry::getValue).collect(Collectors.toList());
            bookRedisCommandPort.saveBookSearchRank(isbns, scores, yesterday);
        }
    }

}
