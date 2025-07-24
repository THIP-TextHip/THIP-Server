package konkuk.thip.feed.application.port.out;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;

import java.time.LocalDateTime;
import java.util.Set;

public interface FeedQueryPort {

    Set<Long> findUserIdsByBookId(Long bookId);

    CursorBasedList<FeedQueryDto> findFeedsByFollowingPriority(Long userId, LocalDateTime cursorVal, int size);

    CursorBasedList<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime cursorVal, int size);
}
