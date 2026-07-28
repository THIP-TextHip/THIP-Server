package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.adapter.out.api.naver.NaverApiClient;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.discord.DiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompositeBookApiAdapter implements BookApiQueryPort {

    private final NaverApiClient naverApiClient;
    private final AladinApiClient aladinApiClient;
    private final DiscordClient discordClient;

    @Override
    public NaverBookParseResult findBooksByKeyword(String keyword, int start) {
        return naverApiClient.findBooksByKeyword(keyword, start);
    }

    @Override
    public NaverDetailBookParseResult findDetailBookByIsbn(String isbn) {
        return naverApiClient.findDetailBookByIsbn(isbn);
    }

    // 알라딘 API 실패 시 Discord 알림 전송 후 null 반환 (방 생성은 계속 진행)
    @Override
    public Integer findPageCountByIsbn(String isbn) {
        try {
            return aladinApiClient.findPageCountByIsbn(isbn);
        } catch (Exception e) {
            log.error("[Aladin API] pageCount 획득 실패 - isbn: {}, error: {}", isbn, e.getMessage());
            discordClient.sendAladinApiFailureAlert(isbn, e);
            return null;
        }
    }

    @Override
    public Book loadBookWithPageByIsbn(String isbn) {
        // 1. Naver API - 실패 시 예외 전파 (방 생성 실패)
        NaverDetailBookParseResult detailBook = findDetailBookByIsbn(isbn);

        // 2. Aladin API - 실패 시 null 반환 (Discord 알림은 findPageCountByIsbn 내부에서 처리)
        Integer pageCount = findPageCountByIsbn(isbn);

        // 3. pageCount가 null이어도 Book 반환 (방 생성 계속 진행)
        return Book.withoutId(
                detailBook.title(),
                isbn,
                detailBook.author(),
                false,      // TODO : 추후 BestSeller 도입되면 고려해야함
                detailBook.publisher(),
                detailBook.imageUrl(),
                pageCount,
                detailBook.description()
        );
    }
}
