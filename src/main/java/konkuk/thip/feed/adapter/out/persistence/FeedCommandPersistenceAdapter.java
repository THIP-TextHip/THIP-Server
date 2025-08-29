package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.*;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class FeedCommandPersistenceAdapter implements FeedCommandPort {

    private final FeedJpaRepository feedJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final SavedFeedJpaRepository savedFeedJpaRepository;

    private final FeedMapper feedMapper;

    @Override
    public Optional<Feed> findById(Long id) {
        return feedJpaRepository.findByPostId(id)
                .map(feedMapper::toDomainEntity);
    }


    @Override
    public Long save(Feed feed) {

        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(feed.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(feed.getTargetBookId()).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );
        FeedJpaEntity feedJpaEntity = feedMapper.toJpaEntity(feed,userJpaEntity,bookJpaEntity);

        // Feed 먼저 영속화 → ID 생성
        FeedJpaEntity savedFeed = feedJpaRepository.save(feedJpaEntity);

        return savedFeed.getPostId();
    }

    @Override
    public Long update(Feed feed) {
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findByPostId(feed.getId())
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
        feedJpaEntity.updateFrom(feed);

        return feedJpaEntity.getPostId();
    }

    @Override
    public void saveSavedFeed(Long userId, Long feedId) {
        UserJpaEntity user = userJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        FeedJpaEntity feed = feedJpaRepository.findByPostId(feedId)
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
        SavedFeedJpaEntity entity = SavedFeedJpaEntity.builder()
                .userJpaEntity(user)
                .feedJpaEntity(feed)
                .build();
        savedFeedJpaRepository.save(entity);
    }

    @Override
    public void deleteSavedFeed(Long userId, Long feedId) {
        savedFeedJpaRepository.deleteByUserIdAndFeedId(userId, feedId);
    }

    @Override
    public void delete(Feed feed) {
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findByPostId(feed.getId())
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));

        savedFeedJpaRepository.deleteAllByFeedId(feedJpaEntity.getPostId());

        feedJpaEntity.softDelete();
        feedJpaRepository.save(feedJpaEntity);
    }
}
