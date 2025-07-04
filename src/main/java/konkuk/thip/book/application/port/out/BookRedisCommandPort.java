package konkuk.thip.book.application.port.out;

import java.time.LocalDate;
import java.util.List;

public interface BookRedisCommandPort {
    void incrementBookSearchCount(String isbn, LocalDate date);
    void saveBookSearchRank(List<String> isbns, List<Double> scores, LocalDate date);
    void deleteBookSearchRank(LocalDate date);
    void deleteBookSearchCount(LocalDate date);
}
