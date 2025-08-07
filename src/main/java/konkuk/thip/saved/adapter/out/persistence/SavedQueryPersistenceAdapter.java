package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.SavedFeeds;
import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.saved.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.saved.application.port.out.dto.FeedIdAndTagProjection;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class SavedQueryPersistenceAdapter implements SavedQueryPort {

    private final SavedFeedJpaRepository savedFeedJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final FeedMapper feedMapper;
    private final FeedTagJpaRepository feedTagJpaRepository;

    @Override
    public SavedFeeds findSavedFeedsByUserId(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        List<SavedFeedJpaEntity> savedFeedEntities =
                savedFeedJpaRepository.findAllByUserId(user.getUserId());

        List<Long> feedIds = savedFeedEntities.stream()
                .map(entity -> entity.getFeedJpaEntity().getPostId())
                .toList();

        // Projection 기반 조회
        List<FeedIdAndTagProjection> results = feedTagJpaRepository.findFeedIdAndTagsByFeedIds(feedIds);

        Map<Long, List<TagJpaEntity>> feedTagsMap = results.stream()
                .collect(Collectors.groupingBy(
                        FeedIdAndTagProjection::getFeedId,
                        Collectors.mapping(FeedIdAndTagProjection::getTagJpaEntity, Collectors.toList())
                ));

        List<Feed> feeds = savedFeedEntities.stream()
                .map(entity -> {
                    FeedJpaEntity feedJpa = entity.getFeedJpaEntity();
                    List<TagJpaEntity> tags = feedTagsMap.getOrDefault(feedJpa.getPostId(), List.of());
                    return feedMapper.toDomainEntity(feedJpa, tags);
                })
                .toList();

        return new SavedFeeds(feeds);
    }

    @Override
    public Set<Long> findSavedFeedIdsByUserIdAndFeedIds(Set<Long> feedIds, Long userId) {
        return savedFeedJpaRepository.findSavedFeedIdsByUserIdAndFeedIds(userId, feedIds);
    }
}
