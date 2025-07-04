package konkuk.thip.book.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.book.domain.SavedBooks;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NOT_FOUND;

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

        // Book 조회 or 생성
        Book book = bookCommandPort.findByIsbn(isbn)
                .orElseGet(() -> {
                    if (!isSave) {
                        // 삭제 요청인데 책이 DB에 없으면 저장하지 않은 책이므로 예외처리
                        throw new BusinessException(BOOK_NOT_FOUND);
                    }
                    // 저장 요청인데 책이 DB에 없으면 네이버 API로 저장
                    NaverDetailBookParseResult naverResult = bookApiQueryPort.findDetailBookByKeyword(isbn);
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
                    return newBook.withId(newBookId);
                });

        //유저가 저장한 책 목록 조회
        SavedBooks savedBooks = savedQueryPort.findByUserId(userId);

        if (isSave) {
            // 저장 요청일 떄는 사용자가 이미 저장하지 않았던 책이면 저장
            savedBooks.validateNotAlreadySaved(book);
            savedCommandPort.saveBook(userId, book.getId());
        } else {
            // 삭제 요청일 때는 사용자가 저장한 책이면 삭제
            savedBooks.validateCanDelete(book);
            savedCommandPort.deleteBook(userId, book.getId());
        }
        return BookIsSavedResult.of(isbn, isSave);
    }
}