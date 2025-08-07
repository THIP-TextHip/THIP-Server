package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.saved.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.saved.application.port.out.SavedCommandPort;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.FEED_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class SavedCommandPersistenceAdapter implements SavedCommandPort {

    private final UserJpaRepository userJpaRepository;
    private final FeedJpaRepository feedJpaRepository;
    private final SavedFeedJpaRepository savedFeedJpaRepository;

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
        savedFeedJpaRepository.deleteByUserIdAndFeedId(userId, feedId);
    }


}