package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface FeedQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);

    List<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Integer lastPriority, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findSpecificUserFeedsByCreatedAt(Long userId, Long feedOwnerId, LocalDateTime lastCreatedAt, int size);

    List<TagCategoryQueryDto> findAllTags();
}
