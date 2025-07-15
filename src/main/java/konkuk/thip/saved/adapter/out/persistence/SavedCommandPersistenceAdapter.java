package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.saved.adapter.out.mapper.SavedBookMapper;
import konkuk.thip.saved.adapter.out.mapper.SavedFeedMapper;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class SavedCommandPersistenceAdapter implements SavedCommandPort {

    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final SavedBookJpaRepository savedBookJpaRepository;
    private final SavedFeedJpaRepository savedFeedJpaRepository;
    private final SavedBookMapper savedBookMapper;
    private final SavedFeedMapper savedFeedMapper;

    @Override
    public void saveBook(Long userId, Long bookId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        BookJpaEntity book = bookJpaRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(BOOK_NOT_FOUND));
        SavedBookJpaEntity entity = SavedBookJpaEntity.builder()
                .userJpaEntity(user)
                .bookJpaEntity(book)
                .build();
        savedBookJpaRepository.save(entity);
    }


    //삭제 전략 도입 전
    @Override
    public void deleteBook(Long userId, Long bookId) {
        savedBookJpaRepository.deleteByUserJpaEntity_UserIdAndBookJpaEntity_BookId(userId, bookId);
    }
}