package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.ROOM_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BookCommandPersistenceAdapter implements BookCommandPort {

    private final RoomJpaRepository roomJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final BookMapper bookMapper;

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookJpaRepository.findByIsbn(isbn)
                .map(bookMapper::toDomainEntity);
    }

    @Override
    public Long save(Book book) {
        BookJpaEntity bookJpaEntity = bookMapper.toJpaEntity(book);
        return bookJpaRepository.save(bookJpaEntity).getBookId();
    }

    @Override
    public Book findById(Long id) {
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );

        return bookMapper.toDomainEntity(bookJpaEntity);
    }

    @Override
    public void updateForPageCount(Book book) {
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(book.getId()).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );

        bookJpaEntity.changePageCount(book.getPageCount());
        bookJpaRepository.save(bookJpaEntity);
    }

    @Override
    public Book findBookByRoomId(Long roomId) {
        BookJpaEntity bookJpaEntity = roomJpaRepository.findById(roomId).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        ).getBookJpaEntity();
        return bookMapper.toDomainEntity(bookJpaEntity);
    }
}
