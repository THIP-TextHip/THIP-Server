package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FeedQueryPersistenceAdapter implements FeedQueryPort {

    private final FeedJpaRepository feedJpaRepository;
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
    public CursorBasedList<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size) {
        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findLatestFeedsByCreatedAt(userId, lastCreatedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> feedQueryDto.createdAt().toString());
    }
}
