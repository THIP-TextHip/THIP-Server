package konkuk.thip.room.application.service;

import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.ExternalApiException;
import konkuk.thip.room.application.port.in.dto.RoomCreateCommand;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NAVER_API_REQUEST_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("[단위] 방 생성 서비스 단위 테스트")
class RoomCreateServiceTest {

    private RoomCommandPort roomCommandPort;
    private RoomParticipantCommandPort roomParticipantCommandPort;
    private BookCommandPort bookCommandPort;
    private BookApiQueryPort bookApiQueryPort;
    private TransactionTemplate transactionTemplate;
    private RoomCreateService roomCreateService;

    private static final Long USER_ID = 1L;
    private static final Long BOOK_ID = 10L;
    private static final Long ROOM_ID = 100L;
    private static final String ISBN = "9791168342941";

    @BeforeEach
    void setUp() {
        roomCommandPort = mock(RoomCommandPort.class);
        roomParticipantCommandPort = mock(RoomParticipantCommandPort.class);
        bookCommandPort = mock(BookCommandPort.class);
        bookApiQueryPort = mock(BookApiQueryPort.class);
        transactionTemplate = mock(TransactionTemplate.class);

        roomCreateService = new RoomCreateService(
                roomCommandPort,
                roomParticipantCommandPort,
                bookCommandPort,
                bookApiQueryPort,
                transactionTemplate
        );

        // TransactionTemplate mock: 콜백을 직접 실행
        given(transactionTemplate.execute(any())).willAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        given(roomCommandPort.save(any())).willReturn(ROOM_ID);
    }

    private RoomCreateCommand createCommand() {
        return new RoomCreateCommand(
                ISBN, "문학", "방이름", "방설명",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                3, null, true
        );
    }

    private Book bookWithPage() {
        return Book.builder()
                .id(BOOK_ID).title("제목").isbn(ISBN).authorName("저자")
                .bestSeller(false).publisher("출판사").imageUrl("img.jpg")
                .pageCount(296).description("설명").build();
    }

    private Book bookWithoutPage() {
        return Book.builder()
                .id(BOOK_ID).title("제목").isbn(ISBN).authorName("저자")
                .bestSeller(false).publisher("출판사").imageUrl("img.jpg")
                .pageCount(null).description("설명").build();
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Case A: DB에 pageCount 있는 Book 존재
    // ────────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DB에 pageCount가 있는 Book이 존재하는 경우")
    class CaseA {

        @Test
        @DisplayName("외부 API 호출 없이 방이 생성된다")
        void createRoom_bookWithPageExists_noApiCall() {
            // given
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.of(bookWithPage()));

            // when
            Long roomId = roomCreateService.createRoom(createCommand(), USER_ID);

            // then
            assertThat(roomId).isEqualTo(ROOM_ID);
            then(bookApiQueryPort).should(never()).findPageCountByIsbn(any());
            then(bookApiQueryPort).should(never()).loadBookWithPageByIsbn(any());
            then(roomCommandPort).should().save(any());
            then(roomParticipantCommandPort).should().save(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Case B: DB에 pageCount 없는 Book 존재
    // ────────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DB에 pageCount가 없는 Book이 존재하는 경우")
    class CaseB {

        @Test
        @DisplayName("알라딘 API 성공 시 pageCount가 업데이트되고 방이 생성된다")
        void createRoom_aladinSuccess_pageCountUpdated() {
            // given
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.of(bookWithoutPage()));
            given(bookApiQueryPort.findPageCountByIsbn(ISBN)).willReturn(296);

            // when
            Long roomId = roomCreateService.createRoom(createCommand(), USER_ID);

            // then
            assertThat(roomId).isEqualTo(ROOM_ID);
            then(bookCommandPort).should().updateForPageCount(any());
            then(roomCommandPort).should().save(any());
        }

        @Test
        @DisplayName("알라딘 API 실패(null 반환) 시 pageCount 업데이트 없이 방이 생성된다")
        void createRoom_aladinFail_roomCreatedWithNullPage() {
            // given
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.of(bookWithoutPage()));
            given(bookApiQueryPort.findPageCountByIsbn(ISBN)).willReturn(null); // 알라딘 실패

            // when
            Long roomId = roomCreateService.createRoom(createCommand(), USER_ID);

            // then: pageCount 업데이트 없이 방 생성 계속 진행
            assertThat(roomId).isEqualTo(ROOM_ID);
            then(bookCommandPort).should(never()).updateForPageCount(any());
            then(roomCommandPort).should().save(any());
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // Case C: DB에 Book 없음
    // ────────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DB에 Book이 없는 경우")
    class CaseC {

        @Test
        @DisplayName("네이버 + 알라딘 API 성공 시 Book이 저장되고 방이 생성된다")
        void createRoom_bookNotExist_savedAndRoomCreated() {
            // given
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.empty());
            given(bookApiQueryPort.loadBookWithPageByIsbn(ISBN)).willReturn(bookWithPage());
            given(bookCommandPort.save(any())).willReturn(BOOK_ID);
            given(roomCommandPort.save(any())).willReturn(ROOM_ID);

            // when
            Long roomId = roomCreateService.createRoom(createCommand(), USER_ID);

            // then
            assertThat(roomId).isEqualTo(ROOM_ID);
            then(bookCommandPort).should().save(any());
            then(roomCommandPort).should().save(any());
        }

        @Test
        @DisplayName("알라딘 API 실패 시 pageCount=null인 Book이 저장되고 방이 생성된다")
        void createRoom_aladinFail_bookSavedWithNullPageAndRoomCreated() {
            // given
            Book bookNullPage = Book.withoutId("제목", ISBN, "저자", false, "출판사", "img.jpg", null, "설명");
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.empty());
            given(bookApiQueryPort.loadBookWithPageByIsbn(ISBN)).willReturn(bookNullPage); // 알라딘 실패 → null pageCount
            given(bookCommandPort.save(any())).willReturn(BOOK_ID);
            given(roomCommandPort.save(any())).willReturn(ROOM_ID);

            // when
            Long roomId = roomCreateService.createRoom(createCommand(), USER_ID);

            // then: Book은 저장되고 방 생성도 계속 진행
            assertThat(roomId).isEqualTo(ROOM_ID);
            then(bookCommandPort).should().save(any());
            then(roomCommandPort).should().save(any());
        }

        @Test
        @DisplayName("네이버 API 장애 시 방 생성이 실패한다")
        void createRoom_naverFail_throwsException() {
            // given
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.empty());
            given(bookApiQueryPort.loadBookWithPageByIsbn(ISBN))
                    .willThrow(new ExternalApiException(BOOK_NAVER_API_REQUEST_ERROR));

            // when & then: 예외 전파 → 방 생성 실패
            assertThatThrownBy(() -> roomCreateService.createRoom(createCommand(), USER_ID))
                    .isInstanceOf(ExternalApiException.class);

            then(bookCommandPort).should(never()).save(any());
            then(roomCommandPort).should(never()).save(any());
        }

        @Test
        @DisplayName("동일 ISBN 동시 요청으로 저장 충돌 시 기존 Book을 재사용해 방이 생성된다")
        void createRoom_concurrentSameIsbn_reuseExistingBook() {
            // given
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.empty());
            given(bookApiQueryPort.loadBookWithPageByIsbn(ISBN)).willReturn(bookWithPage());
            // 첫 번째 save: 동시 요청으로 이미 저장된 Book → DataIntegrityViolationException
            given(bookCommandPort.save(any())).willThrow(new DataIntegrityViolationException("duplicate key"));
            // 재조회 시 이미 저장된 Book 반환
            given(bookCommandPort.findByIsbn(ISBN)).willReturn(Optional.empty(), Optional.of(bookWithPage()));
            given(roomCommandPort.save(any())).willReturn(ROOM_ID);

            // when
            Long roomId = roomCreateService.createRoom(createCommand(), USER_ID);

            // then: 예외 없이 방 생성 성공
            assertThat(roomId).isEqualTo(ROOM_ID);
            then(roomCommandPort).should().save(any());
        }
    }
}
