package konkuk.thip.feed.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.out.persistence.FeedQueryPersistenceAdapter;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위] FeedQueryCacheAdapterTest 단위 테스트")
public class FeedQueryCacheAdapterTest {

    @Mock private FeedCacheHandler feedCacheHandler;
    @Mock private FeedQueryPersistenceAdapter persistenceAdapter;

    @InjectMocks private FeedQueryCacheAdapter cacheAdapter;

    private final Long USER_ID = 1L;

    @Test
    @DisplayName("findLatestFeedsByFeedId: 캐시 범위 안, size 충족하면 캐시 결과 반환하고(캐시 히트) DB를 호출하지 않는다.")
    void return_from_cache_when_hit() {
        // given
        int pageSize = 5;
        Cursor cursor = Cursor.from(null, pageSize);

        when(feedCacheHandler.getTopIds())
                .thenReturn(List.of(50L, 40L, 30L, 20L, 10L));

        when(feedCacheHandler.getFeedDetail(50L))
                .thenReturn(createDto(50L, 2L, true));
        when(feedCacheHandler.getFeedDetail(40L))
                .thenReturn(createDto(40L, 3L, true));
        when(feedCacheHandler.getFeedDetail(30L))
                .thenReturn(createDto(30L, 4L, true));
        when(feedCacheHandler.getFeedDetail(20L))
                .thenReturn(createDto(20L, 5L, true));
        when(feedCacheHandler.getFeedDetail(10L))
                .thenReturn(createDto(10L, 6L, true));

        // when
        CursorBasedList<FeedQueryDto> result =
                cacheAdapter.findLatestFeedsByFeedId(USER_ID, cursor);

        // then
        assertNotNull(result);
        verify(persistenceAdapter, never())
                .findLatestFeedsByFeedId(any(), any());
    }

    @Test
    @DisplayName("findLatestFeedsByFeedId: 캐시 범위 안이지만 size 부족하면(캐시 부분히트) DB로 위임한다.")
    void delegate_to_db_when_cache_insufficient() {
        // given
        int pageSize = 3;
        Cursor cursor = Cursor.from(null, pageSize);

        when(feedCacheHandler.getTopIds())
                .thenReturn(List.of(50L, 40L));   // 보여줘야하는 페이지 사이즈보다 1작음

        when(feedCacheHandler.getFeedDetail(50L))
                .thenReturn(createDto(50L, 2L, true));
        when(feedCacheHandler.getFeedDetail(40L))
                .thenReturn(createDto(40L, 3L, true));

        CursorBasedList<FeedQueryDto> dbResult =
                new CursorBasedList<>(List.of(), null, false);
        when(persistenceAdapter.findLatestFeedsByFeedId(USER_ID, cursor))
                .thenReturn(dbResult);

        // when
        cacheAdapter.findLatestFeedsByFeedId(USER_ID, cursor);

        // then
        verify(persistenceAdapter, times(1))
                .findLatestFeedsByFeedId(USER_ID, cursor);
    }

    @Test
    @DisplayName("findLatestFeedsByFeedId: 캐시가 비어있으면(캐시 미스) DB로 위임한다.")
    void delegate_to_db_when_cache_is_empty() {
        // given
        int pageSize = 2;
        Cursor cursor = Cursor.from(null, pageSize);

        when(feedCacheHandler.getTopIds())
                .thenReturn(List.of());   // 캐시 비어있음 → 바로 DB

        CursorBasedList<FeedQueryDto> dbResult =
                new CursorBasedList<>(List.of(), null, false);
        when(persistenceAdapter.findLatestFeedsByFeedId(USER_ID, cursor))
                .thenReturn(dbResult);

        // when
        cacheAdapter.findLatestFeedsByFeedId(USER_ID, cursor);

        // then
        verify(persistenceAdapter, times(1))
                .findLatestFeedsByFeedId(USER_ID, cursor);
    }

    @Test
    @DisplayName("findLatestFeedsByFeedId: 캐시 범위를 벗어나면(캐시 미스) DB로 위임한다.")
    void delegate_to_db_when_cursor_out_of_cache_range() {
        // given
        int pageSize = 2;

        // 캐시에는 100, 90, 80 있음
        // 커서가 70이면 → 70 > 80 false → 캐시 범위 벗어남
        Cursor cursor = Cursor.from("70", pageSize);

        when(feedCacheHandler.getTopIds())
                .thenReturn(List.of(100L, 90L, 80L));

        CursorBasedList<FeedQueryDto> dbResult =
                new CursorBasedList<>(List.of(), null, false);
        when(persistenceAdapter.findLatestFeedsByFeedId(USER_ID, cursor))
                .thenReturn(dbResult);

        // when
        cacheAdapter.findLatestFeedsByFeedId(USER_ID, cursor);

        // then
        verify(persistenceAdapter, times(1))
                .findLatestFeedsByFeedId(USER_ID, cursor);
    }

    @Test
    @DisplayName("findLatestFeedsByFeedId: 캐시 데이터 필터링이 올바르게 동작한다.(공개글, 내가작성한 비공개글)")
    void filter_logic_should_work_correctly() {
        // given
        int pageSize = 2;
        Cursor cursor = Cursor.from(null, pageSize);
        when(feedCacheHandler.getTopIds())
                .thenReturn(List.of(50L, 40L, 30L, 20L));

        // 50 → 공개 (포함)
        when(feedCacheHandler.getFeedDetail(50L))
                .thenReturn(createDto(50L, 2L, true));
        // 40 → 비공개 + 타인 (제외)
        when(feedCacheHandler.getFeedDetail(40L))
                .thenReturn(createDto(40L, 3L, false));
        // 30 → 비공개 + 본인 (포함)
        when(feedCacheHandler.getFeedDetail(30L))
                .thenReturn(createDto(30L, USER_ID, false));
        // 20 → null (제외)
        when(feedCacheHandler.getFeedDetail(20L))
                .thenReturn(null);

        // when
        CursorBasedList<FeedQueryDto> result =
                cacheAdapter.findLatestFeedsByFeedId(USER_ID, cursor);

        // then
        assertNotNull(result);
        assertThat(result.contents()).hasSize(2);
        assertThat(result.contents())
                .extracting(FeedQueryDto::feedId)
                .containsExactly(50L, 30L);

        verify(persistenceAdapter, never())
                .findLatestFeedsByFeedId(any(), any());
    }

    @Test
    @DisplayName("getFeedDetail: 캐시에 존재하면 DB를 호출하지 않는다.(캐시 히트)")
    void get_detail_from_cache() {
        // given
        when(feedCacheHandler.getFeedDetail(1L))
                .thenReturn(createDto(1L, USER_ID,true));

        // when
        FeedQueryDto result = cacheAdapter.getFeedDetail(1L);

        // then
        assertNotNull(result);
        verify(persistenceAdapter, never())
                .getFeedDetail(any());
    }

    @Test
    @DisplayName("getFeedDetail: 캐시에 없으면 DB를 호출한다.(캐시 미스)")
    void get_detail_from_db_when_cache_miss() {
        // given
        when(feedCacheHandler.getFeedDetail(1L))
                .thenReturn(null);

        FeedQueryDto dbResult = createDto(1L, USER_ID, true);
        when(persistenceAdapter.getFeedDetail(1L))
                .thenReturn(dbResult);

        // when
        FeedQueryDto result = cacheAdapter.getFeedDetail(1L);

        // then
        assertNotNull(result);
        assertThat(result).isEqualTo(dbResult);

        verify(persistenceAdapter, times(1))
                .getFeedDetail(1L);
    }

    private FeedQueryDto createDto(Long feedId, Long creatorId, boolean isPublic) {
        return FeedQueryDto.builder()
                .feedId(feedId)
                .creatorId(creatorId)
                .creatorNickname("유저")
                .creatorProfileImageUrl("/profile_science.png")
                .alias("과학자")
                .createdAt(LocalDateTime.now())
                .isbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13))
                .bookTitle("테스트 책")
                .bookAuthor("테스트 저자")
                .contentBody("기본 피드 본문입니다.")
                .contentUrls(new String[]{})
                .likeCount(0)
                .commentCount(0)
                .isPublic(isPublic)
                .isPriorityFeed(false)
                .savedCreatedAt(null)
                .build();
    }
}
