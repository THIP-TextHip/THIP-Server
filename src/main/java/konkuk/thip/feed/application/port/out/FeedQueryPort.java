package konkuk.thip.feed.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.domain.SavedFeeds;

import java.util.List;
import java.util.Set;

public interface FeedQueryPort {

    Set<Long> findUserIdsByBookId(Long bookId);

    /**
     * 전체 피드 조회
     */
    CursorBasedList<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Cursor cursor);
    CursorBasedList<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, Cursor cursor);

    /**
     * 내 피드 조회
     */
    CursorBasedList<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, Cursor cursor);

    /**
     * 특정 유저 피드 조회
     */
    CursorBasedList<FeedQueryDto> findSpecificUserFeedsByCreatedAt(Long feedOwnerId, Cursor cursor);

    int countAllFeedsByUserId(Long userId);

    int countPublicFeedsByUserId(Long userId);

    /**
     * 저장된 피드 조회
     */
    SavedFeeds findSavedFeedsByUserId(Long userId);

    Set<Long> findSavedFeedIdsByUserIdAndFeedIds(Set<Long> feedIds, Long userId);

    List<TagCategoryQueryDto> findAllTags();

    /**
     * 특정 책으로 작성된 피드 조회
     */
    CursorBasedList<FeedQueryDto> findFeedsByBookIsbnOrderByLike(String isbn, Long userId, Cursor cursor);

    CursorBasedList<FeedQueryDto> findFeedsByBookIsbnOrderByLatest(String isbn, Long userId, Cursor cursor);
}
