package konkuk.thip.book.application.service;

import konkuk.thip.book.application.port.in.BookCleanUpUseCase;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCleanUpService implements BookCleanUpUseCase {

    private final BookCommandPort bookCommandPort;
    private final BookQueryPort bookQueryPort;

    @Async("schedulerAsyncExecutor")
    @Override
    @Transactional
    public void deleteUnusedBooks() {
        Set<Long> unusedBookIds = bookQueryPort.findUnusedBookIds();
        log.info("삭제할 사용되지 않는 Book IDs: {}", unusedBookIds);
        bookCommandPort.deleteAllByIdInBatch(unusedBookIds);
    }
}
