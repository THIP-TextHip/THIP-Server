package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedTag.FeedTagJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import konkuk.thip.feed.application.port.out.dto.FeedIdAndTagProjection;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.SavedFeeds;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class FeedQueryPersistenceAdapter implements FeedQueryPort {

    private final FeedJpaRepository feedJpaRepository;
    private final FeedTagJpaRepository feedTagJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final SavedFeedJpaRepository savedFeedJpaRepository;
    private final FeedMapper feedMapper;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        return feedJpaRepository.findUserIdsByBookId(bookId);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Cursor cursor) {
        Integer lastPriority = cursor.isFirstRequest() ? null : cursor.getInteger(0);
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(1);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findFeedsByFollowingPriority(userId, lastPriority, lastCreatedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(
                    Boolean.TRUE.equals(feedQueryDto.isPriorityFeed()) ? "1" : "0",
                    feedQueryDto.createdAt().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, Cursor cursor) {
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findLatestFeedsByCreatedAt(userId, lastCreatedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(feedQueryDto.createdAt().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, Cursor cursor) {
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findMyFeedsByCreatedAt(userId, lastCreatedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(feedQueryDto.createdAt().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<FeedQueryDto> findSpecificUserFeedsByCreatedAt(Long feedOwnerId, Cursor cursor) {
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findSpecificUserFeedsByCreatedAt(feedOwnerId, lastCreatedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(feedQueryDto.createdAt().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public int countAllFeedsByUserId(Long userId) {
        // int 로 강제 형변환 해도 괜찮죠??
        return (int) feedJpaRepository.countAllFeedsByUserId(userId, StatusType.ACTIVE);
    }

    @Override
    public int countPublicFeedsByUserId(Long userId) {
        return (int) feedJpaRepository.countPublicFeedsByUserId(userId, StatusType.ACTIVE);
    }

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

    @Override
    public List<TagCategoryQueryDto> findAllTags() {
        return feedJpaRepository.findAllTags();
    }
}
