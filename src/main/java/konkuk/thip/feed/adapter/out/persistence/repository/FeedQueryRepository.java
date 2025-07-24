package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface FeedQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);

    List<FeedQueryDto> findFeedsByFollowingPriority(Long userId, LocalDateTime cursorVal, int size);

    List<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime cursorVal, int size);
}