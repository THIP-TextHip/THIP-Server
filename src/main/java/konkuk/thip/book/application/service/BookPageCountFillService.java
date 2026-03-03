package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.application.port.in.BookPageCountFillUseCase;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.discord.DiscordClient;
import konkuk.thip.common.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ALADIN_API_ISBN_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookPageCountFillService implements BookPageCountFillUseCase {

    @Value("${scheduler.throttle-ms:500}") // 초당 2건 → 알라딘 API 과부하 방지
    private long throttleMs;

    private final BookQueryPort bookQueryPort;
    private final BookCommandPort bookCommandPort;
    private final AladinApiClient aladinApiClient;
    private final DiscordClient discordClient;

    // @Transactional 미적용 - 의도적 설계
    // 각 book의 pageCount 업데이트는 독립적인 작업이므로, 일부 실패 시 나머지 성공 건은 유지되어야 함
    // 전체를 하나의 트랜잭션으로 묶으면 하나의 실패가 전체 롤백을 유발하고,
    // 수백~수천 건 처리 중 DB 커넥션을 장시간 점유하게 되어 비효율적
    @Override
    public void fillNullPageCounts() {
        List<Book> books = bookQueryPort.findBooksWithNullPageCountLinkedToRooms();

        if (books.isEmpty()) {
            log.info("[스케줄러] pageCount 업데이트 대상 없음");
            return;
        }

        int successCount = 0;
        int unfindableCount = 0;
        int transientFailCount = 0;

        for (Book book : books) {
            try {
                Integer pageCount = aladinApiClient.findPageCountByIsbn(book.getIsbn());
                book.changePageCount(pageCount);
                bookCommandPort.updateForPageCount(book);
                successCount++;
                log.info("[스케줄러] pageCount 업데이트 완료 - isbn: {}, pageCount: {}", book.getIsbn(), pageCount);
            } catch (ExternalApiException e) {
                if (e.getErrorCode() == BOOK_ALADIN_API_ISBN_NOT_FOUND) {
                    // 영구 실패: 알라딘에 미등록된 책 → 재시도 불필요
                    book.markAsUnfindable();
                    bookCommandPort.updateForUnfindable(book);
                    unfindableCount++;
                    log.info("[스케줄러] 알라딘 미등록 book 처리 완료 - isbn: {}", book.getIsbn());
                } else {
                    // 파싱 오류 등 그 외 ExternalApiException → 일시 장애로 간주, 다음 실행에서 재시도
                    transientFailCount++;
                    log.warn("[스케줄러] 알라딘 API 오류 (재시도 예정) - isbn: {}, error: {}", book.getIsbn(), e.getMessage());
                }
            } catch (Exception e) {
                // 타임아웃(ResourceAccessException) 등 일시 장애 → 다음 실행에서 재시도
                transientFailCount++;
                log.warn("[스케줄러] 일시 장애 (재시도 예정) - isbn: {}, error: {}", book.getIsbn(), e.getMessage());
            }

            throttle();
        }

        log.info("[스케줄러] 실행 완료 - 대상: {}건 / 성공: {}건 / 미등록: {}건 / 일시실패: {}건",
                books.size(), successCount, unfindableCount, transientFailCount);
        discordClient.sendPageCountFillResult(books.size(), successCount, unfindableCount, transientFailCount);
    }

    private void throttle() {
        try {
            Thread.sleep(throttleMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
