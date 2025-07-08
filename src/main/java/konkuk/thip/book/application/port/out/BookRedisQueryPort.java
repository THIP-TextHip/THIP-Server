package konkuk.thip.book.application.port.out;

import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookRedisQueryPort {
    List<Map.Entry<String, Double>> getBookSearchRank(LocalDate date, int topN);
    List<Map.Entry<String, Double>> getBookSearchCountTopN(LocalDate date, int topN);
    List<BookMostSearchResult.BookRankInfo> getYesterdayBookRankInfos(LocalDate date);
}
