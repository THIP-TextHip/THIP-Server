package konkuk.thip.book.application.port.out;

import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;

import java.time.LocalDate;
import java.util.List;

public interface BookRedisCommandPort {
    void incrementBookSearchCount(String isbn, LocalDate date);
    void saveBookSearchRank(List<String> isbns, List<Double> scores, LocalDate date);
    void saveBookSearchRankDetail(List<BookMostSearchResult.BookRankInfo> bookRankDetails, LocalDate date);
    void deleteBookSearchRank(LocalDate date);
    void deleteBookSearchCount(LocalDate date);
    void deleteBookSearchRankDetail(LocalDate date);
}
