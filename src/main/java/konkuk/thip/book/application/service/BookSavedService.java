package konkuk.thip.book.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.book.domain.SavedBooks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NOT_SAVED_DB_CANNOT_DELETE;

@Service
@RequiredArgsConstructor
public class BookSavedService implements BookSavedUseCase {

    private final BookApiQueryPort bookApiQueryPort;
    private final BookCommandPort bookCommandPort;
    private final SavedCommandPort savedCommandPort;
    private final SavedQueryPort savedQueryPort;

    @Override
    @Transactional
    public BookIsSavedResult changeSavedBook(String isbn, boolean isSave, Long userId) {

        Book book;

        try {
            // Book 조회 시도
            book = bookCommandPort.findByIsbn(isbn);
        } catch (EntityNotFoundException e) {
            // 책이 DB에 없을 때 처리

            if (!isSave) {
                // 삭제 요청인데 책이 없으면 저장하지 않은 책이므로 예외 처리
                throw new BusinessException(BOOK_NOT_SAVED_DB_CANNOT_DELETE);
            }

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

            Long newBookId = bookCommandPort.save(newBook);

            book = bookCommandPort.findById(newBookId);
        }

        // 유저가 저장한 책 목록 조회
        SavedBooks savedBooks = savedQueryPort.findByUserId(userId);

        if (isSave) {
            // 저장 요청 시 이미 저장되어 있으면 예외 발생
            savedBooks.validateNotAlreadySaved(book);
            savedCommandPort.saveBook(userId, book.getId());
        } else {
            // 삭제 요청 시 저장되어 있지 않으면 예외 발생
            savedBooks.validateCanDelete(book);
            savedCommandPort.deleteBook(userId, book.getId());
        }

        return BookIsSavedResult.of(isbn, isSave);
    }

}