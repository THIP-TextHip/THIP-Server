package konkuk.thip.book.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BookSavedService implements BookSavedUseCase {

    private final BookApiQueryPort bookApiQueryPort;
    private final BookCommandPort bookCommandPort;
    private final BookQueryPort bookQueryPort;

    @Override
    @Transactional
    public BookIsSavedResult changeSavedBook(String isbn, boolean isSaveRequest, Long userId) {

        // Book 조회 시도
        Book book = bookCommandPort.findByIsbn(isbn)
                .orElseGet(() -> {
                    if (!isSaveRequest) { // 삭제 요청인데 책이 없으면 저장하지 않은 책이므로 예외 처리
                        throw new BusinessException(BOOK_NOT_SAVED_DB_CANNOT_DELETE);
                    }
                    return registerBookByIsbn(isbn);
                });

        boolean alreadySaved = bookQueryPort.existsSavedBookByUserIdAndBookId(userId, book.getId());
        validateSaveBookAction(isSaveRequest, alreadySaved);

        if (isSaveRequest) {
            bookCommandPort.saveSavedBook(userId, book.getId());
        } else {
            bookCommandPort.deleteSavedBook(userId, book.getId());
        }

        return BookIsSavedResult.of(isbn, isSaveRequest);
    }

    private Book registerBookByIsbn(String isbn) {
        // 저장 요청이면 네이버 API로 책 정보 조회 후 저장
        NaverDetailBookParseResult naverResult = bookApiQueryPort.findDetailBookByIsbn(isbn);
        Book newBook = Book.withoutId(
                naverResult.title(),
                naverResult.isbn(),
                naverResult.author(),
                false,
                naverResult.publisher(),
                naverResult.imageUrl(),
                null,
                naverResult.description());

        Long savedBookId = bookCommandPort.save(newBook);
        return bookCommandPort.findById(savedBookId);
    }

    private void validateSaveBookAction(boolean isSaveRequest, boolean alreadySaved) {
        if (isSaveRequest && alreadySaved) {
            // 이미 저장되어 있는 책을 다시 저장하려는 경우 예외 처리
            throw new InvalidStateException(BOOK_ALREADY_SAVED);
        } else if (!isSaveRequest && !alreadySaved) {
            // 저장되지 않은 책을 삭제하려는 경우 예외 처리
            throw new InvalidStateException(BOOK_NOT_SAVED_CANNOT_DELETE);
        }
    }

}