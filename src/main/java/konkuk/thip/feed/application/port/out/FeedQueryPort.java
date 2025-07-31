package konkuk.thip.feed.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;

import java.util.Set;

public interface FeedQueryPort {

    Set<Long> findUserIdsByBookId(Long bookId);

    CursorBasedList<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Cursor cursor);

    CursorBasedList<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, Cursor cursor);

    CursorBasedList<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, Cursor cursor);

    int countAllFeedsByUserId(Long userId);

    int countPublicFeedsByUserId(Long userId);
}
