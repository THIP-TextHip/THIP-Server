package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BookQueryPersistenceAdapter implements BookQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final SavedBookJpaRepository savedBookJpaRepository;
    private final BookMapper bookMapper;

    @Override
    public boolean existsSavedBookByUserIdAndBookId(Long userId, Long bookId) {
        return savedBookJpaRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Override
    public boolean existsBookByIsbn(String isbn) {
        return bookJpaRepository.existsByIsbn(isbn);
    }

    @Override
    public List<Book> findSavedBooksByUserId(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return bookJpaRepository.findSavedBooksByUserId(user.getUserId()).stream()
                .map(bookMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findJoiningRoomsBooksByUserId(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return bookJpaRepository.findJoiningRoomsBooksByUserId(user.getUserId())
                .stream()
                .map(bookMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}
