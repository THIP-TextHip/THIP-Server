package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.discord.DiscordClient;
import konkuk.thip.common.exception.ExternalApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ALADIN_API_ISBN_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ALADIN_API_PARSING_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BookPageCountFillServiceTest {

    // throttleMs는 @Value로 주입되며, Mockito 환경에서는 Spring이 주입하지 않으므로
    // long 기본값 0L이 적용됨 → Thread.sleep(0) → 테스트 지연 없음

    @Mock BookQueryPort bookQueryPort;
    @Mock BookCommandPort bookCommandPort;
    @Mock AladinApiClient aladinApiClient;
    @Mock DiscordClient discordClient;

    @InjectMocks BookPageCountFillService sut;

    private Book bookWithIsbn(String isbn) {
        return Book.builder()
                .isbn(isbn)
                .build();
    }

    @Nested
    @DisplayName("업데이트 대상 없음")
    class WhenNoBooksToUpdate {

        @Test
        @DisplayName("조회된 book이 없으면 API 호출 없이 조기 종료한다")
        void fillNullPageCounts_emptyList_noApiCallsNorDiscordAlert() {
            given(bookQueryPort.findBooksWithNullPageCountLinkedToRooms()).willReturn(List.of());

            sut.fillNullPageCounts();

            then(aladinApiClient).shouldHaveNoInteractions();
            then(discordClient).shouldHaveNoInteractions();
            then(bookCommandPort).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("알라딘 API 성공")
    class WhenAladinApiSucceeds {

        @Test
        @DisplayName("pageCount를 정상 수신하면 book을 업데이트하고 Discord 성공 알림을 보낸다")
        void fillNullPageCounts_success_updatesPageCountAndNotifiesDiscord() {
            Book book = bookWithIsbn("9788966261024");
            given(bookQueryPort.findBooksWithNullPageCountLinkedToRooms()).willReturn(List.of(book));
            given(aladinApiClient.findPageCountByIsbn("9788966261024")).willReturn(300);

            sut.fillNullPageCounts();

            then(bookCommandPort).should().updateForPageCount(book);
            then(bookCommandPort).should(never()).updateForUnfindable(any());
            then(discordClient).should().sendPageCountFillResult(1, 1, 0, 0);
        }
    }

    @Nested
    @DisplayName("알라딘 API 영구 실패 (ISBN 미등록)")
    class WhenIsbnNotFound {

        @Test
        @DisplayName("ISBN_NOT_FOUND면 unfindable 처리하고 Discord 미등록 알림을 보낸다")
        void fillNullPageCounts_isbnNotFound_marksAsUnfindableAndNotifiesDiscord() {
            Book book = bookWithIsbn("0000000000000");
            given(bookQueryPort.findBooksWithNullPageCountLinkedToRooms()).willReturn(List.of(book));
            given(aladinApiClient.findPageCountByIsbn("0000000000000"))
                    .willThrow(new ExternalApiException(BOOK_ALADIN_API_ISBN_NOT_FOUND));

            sut.fillNullPageCounts();

            then(bookCommandPort).should().updateForUnfindable(book);
            then(bookCommandPort).should(never()).updateForPageCount(any());
            then(discordClient).should().sendPageCountFillResult(1, 0, 1, 0);
        }
    }

    @Nested
    @DisplayName("알라딘 API 일시 장애 (재시도 예정)")
    class WhenTransientFailure {

        @Test
        @DisplayName("파싱 오류(ExternalApiException)면 DB 업데이트 없이 Discord 일시실패 알림을 보낸다")
        void fillNullPageCounts_parsingError_skipsDbUpdateAndNotifiesDiscord() {
            Book book = bookWithIsbn("9788966261024");
            given(bookQueryPort.findBooksWithNullPageCountLinkedToRooms()).willReturn(List.of(book));
            given(aladinApiClient.findPageCountByIsbn("9788966261024"))
                    .willThrow(new ExternalApiException(BOOK_ALADIN_API_PARSING_ERROR));

            sut.fillNullPageCounts();

            then(bookCommandPort).shouldHaveNoInteractions();
            then(discordClient).should().sendPageCountFillResult(1, 0, 0, 1);
        }

        @Test
        @DisplayName("네트워크 타임아웃(ResourceAccessException)이면 DB 업데이트 없이 Discord 일시실패 알림을 보낸다")
        void fillNullPageCounts_networkTimeout_skipsDbUpdateAndNotifiesDiscord() {
            Book book = bookWithIsbn("9788966261024");
            given(bookQueryPort.findBooksWithNullPageCountLinkedToRooms()).willReturn(List.of(book));
            given(aladinApiClient.findPageCountByIsbn("9788966261024"))
                    .willThrow(new ResourceAccessException("Read timed out"));

            sut.fillNullPageCounts();

            then(bookCommandPort).shouldHaveNoInteractions();
            then(discordClient).should().sendPageCountFillResult(1, 0, 0, 1);
        }
    }

    @Nested
    @DisplayName("혼합 케이스")
    class WhenMixedResults {

        @Test
        @DisplayName("성공/미등록/일시실패가 섞이면 Discord에 각 케이스별 정확한 카운트를 전달한다")
        void fillNullPageCounts_mixedResults_discordReceivesCorrectCounts() {
            Book successBook = bookWithIsbn("1111111111111");
            Book unfindableBook = bookWithIsbn("2222222222222");
            Book transientBook = bookWithIsbn("3333333333333");

            given(bookQueryPort.findBooksWithNullPageCountLinkedToRooms())
                    .willReturn(List.of(successBook, unfindableBook, transientBook));
            given(aladinApiClient.findPageCountByIsbn("1111111111111")).willReturn(250);
            given(aladinApiClient.findPageCountByIsbn("2222222222222"))
                    .willThrow(new ExternalApiException(BOOK_ALADIN_API_ISBN_NOT_FOUND));
            given(aladinApiClient.findPageCountByIsbn("3333333333333"))
                    .willThrow(new ResourceAccessException("Read timed out"));

            sut.fillNullPageCounts();

            then(bookCommandPort).should().updateForPageCount(successBook);
            then(bookCommandPort).should().updateForUnfindable(unfindableBook);
            then(bookCommandPort).should(never()).updateForPageCount(unfindableBook);
            then(bookCommandPort).should(never()).updateForUnfindable(transientBook);
            then(discordClient).should().sendPageCountFillResult(3, 1, 1, 1);
        }
    }
}
