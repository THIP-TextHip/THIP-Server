package konkuk.thip.book.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.adapter.in.web.request.PostBookIsSavedRequest;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.in.BookSavedUseCase;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NOT_SAVED_CANNOT_DELETE;

@Service
@RequiredArgsConstructor
public class BookSavedService implements BookSavedUseCase {

    private final BookApiQueryPort bookApiQueryPort;
    private final BookCommandPort bookCommandPort;
    private final SavedCommandPort savedCommandPort;

    @Override
    @Transactional
    public BookIsSavedResult isSavedBook(String isbn, PostBookIsSavedRequest postBookIsSavedRequest, Long userId) {

        Optional<Book> bookOpt = bookCommandPort.findByIsbn(isbn);

        if (postBookIsSavedRequest.type()) {
            // 저장 요청일 때만 책이 없으면 네이버 API로 저장
            Long bookId = bookOpt.map(Book::getId).orElseGet(() -> {
                NaverDetailBookParseResult naverDetailBookParseResult = bookApiQueryPort.findDetailBookByKeyword(isbn);
                return bookCommandPort.save(NaverDetailBookParseResult.toBook(naverDetailBookParseResult));
            });
            savedCommandPort.saveBook(userId, bookId);
        } else {
            // 삭제 요청일 때는 책이 DB에 있을 때만 삭제 시도
            if (bookOpt.isPresent()) {
                savedCommandPort.deleteBook(userId, bookOpt.get().getId());
            } else {
                // 책이 DB에 없으면 저장하지 않은 책이므로 예외처리
                throw new BusinessException(BOOK_NOT_SAVED_CANNOT_DELETE);
            }
        }

        return BookIsSavedResult.of(isbn,postBookIsSavedRequest.type());
    }
}