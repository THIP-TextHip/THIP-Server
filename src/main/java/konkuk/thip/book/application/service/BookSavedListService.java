package konkuk.thip.book.application.service;

import konkuk.thip.book.application.mapper.BookQueryMapper;
import konkuk.thip.book.application.port.in.BookShowSavedListUseCase;
import konkuk.thip.book.application.port.in.dto.BookShowSavedInfoResult;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSavedListService implements BookShowSavedListUseCase {

    private final BookQueryPort bookQueryPort;
    private final BookQueryMapper bookQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BookShowSavedInfoResult> getSavedBookList(Long userId) {
        List<Book> savedBookList = bookQueryPort.findSavedBooksByUserId(userId);
        return bookQueryMapper.toBookShowSavedInfoResultList(savedBookList);
    }
}
