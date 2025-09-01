package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FeedQueryPersistenceAdapter implements FeedQueryPort {

    private final FeedJpaRepository feedJpaRepository;
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
        return (int) feedJpaRepository.countAllFeedsByUserId(userId);
    }

    @Override
    public int countPublicFeedsByUserId(Long userId) {
        return (int) feedJpaRepository.countPublicFeedsByUserId(userId);
    }

    @Override
    public Set<Long> findSavedFeedIdsByUserIdAndFeedIds(Set<Long> feedIds, Long userId) {
        return savedFeedJpaRepository.findSavedFeedIdsByUserIdAndFeedIds(userId, feedIds);
    }

    @Override
    public boolean existsSavedFeedByUserIdAndFeedId(Long userId, Long feedId) {
        return savedFeedJpaRepository.existsByUserIdAndFeedId(userId, feedId);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findSavedFeedsBySavedAt(Long userId, Cursor cursor) {
        LocalDateTime lastSavedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findSavedFeedsByCreatedAt(userId, lastSavedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(feedQueryDto.savedCreatedAt().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<FeedQueryDto> findFeedsByBookIsbnOrderByLike(String isbn, Long userId, Cursor cursor) {
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        Integer lastLikeCount = cursor.isFirstRequest() ? null : cursor.getInteger(1);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findFeedsByBookIsbnOrderByLikeCount(isbn, userId, lastCreatedAt, lastLikeCount, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(feedQueryDto.createdAt().toString(),
                    feedQueryDto.likeCount().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<FeedQueryDto> findFeedsByBookIsbnOrderByLatest(String isbn, Long userId, Cursor cursor) {
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<FeedQueryDto> feedQueryDtos = feedJpaRepository.findFeedsByBookIsbnOrderByCreatedAt(isbn, userId, lastCreatedAt, size);

        return CursorBasedList.of(feedQueryDtos, size, feedQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(feedQueryDto.createdAt().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public List<Long> findLatestPublicFeedCreatorsIn(Set<Long> userIds, int size) {
        return feedJpaRepository.findLatestPublicFeedCreatorsIn(userIds, size);
    }
}
