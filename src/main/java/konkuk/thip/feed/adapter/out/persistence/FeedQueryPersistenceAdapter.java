package konkuk.thip.feed.adapter.out.persistence;

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
    public CursorBasedList<FeedQueryDto> findFeedsByFollowingPriority(Long userId, LocalDateTime cursorVal, int size) {
        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findFeedsByFollowingPriority(userId, cursorVal, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> feedQueryDto.createdAt().toString());
    }

    @Override
    public CursorBasedList<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime cursorVal, int size) {
        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findLatestFeedsByCreatedAt(userId, cursorVal, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> feedQueryDto.createdAt().toString());
    }
}
