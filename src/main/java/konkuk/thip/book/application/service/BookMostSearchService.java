package konkuk.thip.book.application.service;

import konkuk.thip.book.application.port.in.BookMostSearchUseCase;
import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
import konkuk.thip.book.application.port.out.BookRedisQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookMostSearchService implements BookMostSearchUseCase {

    private final BookRedisQueryPort bookRedisQueryPort;
    private final UserCommandPort userCommandPort;

    @Override
    public BookMostSearchResult getMostSearchedBooks(Long userId) {

        userCommandPort.findById(userId);

        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<BookMostSearchResult.BookRankInfo> bookRankInfos = bookRedisQueryPort.getYesterdayBookRankInfos(yesterday);
        return BookMostSearchResult.of(bookRankInfos);

    }
}