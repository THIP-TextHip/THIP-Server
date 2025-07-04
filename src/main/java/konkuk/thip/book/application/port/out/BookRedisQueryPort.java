package konkuk.thip.book.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookRedisQueryPort {
    List<Map.Entry<String, Double>> getBookSearchRank(LocalDate date, int topN);
    List<Map.Entry<String, Double>> getBookSearchCountTopN(LocalDate date, int topN);
}
