package konkuk.thip.common.scheduler;

import konkuk.thip.book.application.port.in.BookPageCountFillUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookPageCountFillScheduler {

    private final BookPageCountFillUseCase bookPageCountFillUseCase;

    // 매일 새벽 3시 실행 (BookDeleteScheduler(4시)보다 먼저 수행)
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void fillNullPageCounts() {
        log.info("[스케줄러] pageCount 업데이트 시작");
        bookPageCountFillUseCase.fillNullPageCounts();
        log.info("[스케줄러] pageCount 업데이트 종료");
    }
}