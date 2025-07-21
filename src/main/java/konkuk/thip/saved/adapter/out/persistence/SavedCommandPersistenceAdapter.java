package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.saved.adapter.out.mapper.SavedBookMapper;
import konkuk.thip.saved.adapter.out.mapper.SavedFeedMapper;
import konkuk.thip.saved.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.saved.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class SavedCommandPersistenceAdapter implements SavedCommandPort {

    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final FeedJpaRepository feedJpaRepository;
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

    @Override
    public void deleteBook(Long userId, Long bookId) {
        savedBookJpaRepository.deleteByUserJpaEntity_UserIdAndBookJpaEntity_BookId(userId, bookId);
    }

    @Override
    public void saveFeed(Long userId, Long feedId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        FeedJpaEntity feed = feedJpaRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
        SavedFeedJpaEntity entity = SavedFeedJpaEntity.builder()
                .userJpaEntity(user)
                .feedJpaEntity(feed)
                .build();
        savedFeedJpaRepository.save(entity);
    }

    @Override
    public void deleteFeed(Long userId, Long feedId) {
        savedFeedJpaRepository.deleteByUserJpaEntity_UserIdAndFeedJpaEntity_PostId(userId, feedId);
    }


}