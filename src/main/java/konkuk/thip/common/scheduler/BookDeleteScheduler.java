package konkuk.thip.common.scheduler;

import konkuk.thip.book.application.port.in.BookCleanUpUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookDeleteScheduler {

    private final BookCleanUpUseCase bookCleanUpUseCase;

    // 매일 새벽 4시 실행
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void cleanUpUnusedBooks() {
        log.info("[스케줄러] 사용되지 않는 Book 데이터 삭제 시작");
        bookCleanUpUseCase.deleteUnusedBooks();
        log.info("[스케줄러] 사용되지 않는 Book 데이터 삭제 완료");
    }
}