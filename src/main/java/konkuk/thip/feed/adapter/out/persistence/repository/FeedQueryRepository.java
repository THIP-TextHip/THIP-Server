package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface FeedQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);

    List<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Integer lastPriority, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findSpecificUserFeedsByCreatedAt(Long feedOwnerId, LocalDateTime lastCreatedAt, int size);

    List<FeedQueryDto> findFeedsByBookIsbnOrderByLikeCount(String isbn, Long userId, LocalDateTime lastCreatedAt, Integer lastLikeCount, int size);

    List<FeedQueryDto> findFeedsByBookIsbnOrderByCreatedAt(String isbn, Long userId, LocalDateTime lastCreatedAt, int size);

    List<Long> findLatestPublicFeedCreatorsIn(Set<Long> userIds, int size);

    List<FeedQueryDto> findSavedFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size);

    PostQueryDto getPostQueryDtoByPostId(Long postId);
}
