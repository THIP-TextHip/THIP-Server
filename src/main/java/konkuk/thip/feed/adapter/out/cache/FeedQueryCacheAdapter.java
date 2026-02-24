package konkuk.thip.feed.adapter.out.cache;

import java.util.List;
import java.util.Set;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.out.persistence.FeedQueryPersistenceAdapter;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
@RequiredArgsConstructor
public class FeedQueryCacheAdapter implements FeedQueryPort {

    private final FeedCacheHandler feedCacheHandler;
    private final FeedQueryPersistenceAdapter persistenceAdapter;

    @Override
    public CursorBasedList<FeedQueryDto> findLatestFeedsByFeedId(Long userId, Cursor cursor) {
        Long lastPostId = cursor.isFirstRequest() ? Long.MAX_VALUE : cursor.getLong(0);
        int size = cursor.getPageSize();

        // 1. 인덱스 캐시 확인
        List<Long> topIds = feedCacheHandler.getTopIds();

        // 2. 캐시 범위 안의 데이터인지 확인
        if (!topIds.isEmpty() && lastPostId > topIds.get(topIds.size() - 1)) {

            // 캐시에서 조건에 맞는 데이터 필터링
            List<FeedQueryDto> feedQueryDtos = topIds.stream()
                    .filter(id -> id < lastPostId)
                    .map(feedCacheHandler::getFeedDetail)
                    .filter(dto -> dto != null && (dto.isPublic() || dto.creatorId().equals(userId)))
                    .limit(size + 1)
                    .toList();

            if (feedQueryDtos.size() >= size) {
                return CursorBasedList.of(feedQueryDtos, size, dto ->
                        new Cursor(List.of(dto.feedId().toString())).toEncodedString());
            }
        }

        // 3. 캐시 범위를 벗어나면 DB 어댑터로 위임
        return persistenceAdapter.findLatestFeedsByFeedId(userId, cursor);
    }

    @Override
    public FeedQueryDto getFeedDetail(Long feedId) {
        FeedQueryDto cachedDetail = feedCacheHandler.getFeedDetail(feedId);

        if (cachedDetail != null) {
            return cachedDetail;
        }

        return persistenceAdapter.getFeedDetail(feedId);
    }

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        return persistenceAdapter.findUserIdsByBookId(bookId);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Cursor cursor) {
        return persistenceAdapter.findFeedsByFollowingPriority(userId, cursor);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, Cursor cursor) {
        return persistenceAdapter.findMyFeedsByCreatedAt(userId, cursor);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findSpecificUserFeedsByCreatedAt(Long feedOwnerId, Cursor cursor) {
        return persistenceAdapter.findSpecificUserFeedsByCreatedAt(feedOwnerId, cursor);
    }

    @Override
    public int countAllFeedsByUserId(Long userId) {
        return persistenceAdapter.countAllFeedsByUserId(userId);
    }

    @Override
    public int countPublicFeedsByUserId(Long userId) {
        return persistenceAdapter.countPublicFeedsByUserId(userId);
    }

    @Override
    public Set<Long> findSavedFeedIdsByUserIdAndFeedIds(Set<Long> feedIds, Long userId) {
        return persistenceAdapter.findSavedFeedIdsByUserIdAndFeedIds(feedIds, userId);
    }

    @Override
    public boolean existsSavedFeedByUserIdAndFeedId(Long userId, Long feedId) {
        return persistenceAdapter.existsSavedFeedByUserIdAndFeedId(userId, feedId);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findSavedFeedsBySavedAt(Long userId, Cursor cursor) {
        return persistenceAdapter.findSavedFeedsBySavedAt(userId, cursor);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findFeedsByBookIsbnOrderByLike(String isbn, Long userId, Cursor cursor) {
        return persistenceAdapter.findFeedsByBookIsbnOrderByLike(isbn, userId, cursor);
    }

    @Override
    public CursorBasedList<FeedQueryDto> findFeedsByBookIsbnOrderByLatest(String isbn, Long userId, Cursor cursor) {
        return persistenceAdapter.findFeedsByBookIsbnOrderByLatest(isbn, userId, cursor);
    }

    @Override
    public List<Long> findLatestPublicFeedCreatorsIn(Set<Long> userIds, int size) {
        return persistenceAdapter.findLatestPublicFeedCreatorsIn(userIds, size);
    }
}