package konkuk.thip.feed.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위] FeedCacheHandlerTest 단위 테스트")
public class FeedCacheHandlerTest {

    @Mock FeedJpaRepository feedJpaRepository;
    @Mock CacheManager cacheManager;
    @Mock Cache topCache;
    @Mock Cache detailCache;

    @InjectMocks FeedCacheHandler handler;

    private static final int DEFAULT_CACHE_SIZE = 100;

    @Test
    @DisplayName("updateTopIdsWithNewId: 기존 캐시가 있으면 맨 앞에 추가하고 size 초과 시 제거한다.")
    void update_top_ids_success() {
        // given
        String cacheKey = "top" + DEFAULT_CACHE_SIZE;
        List<Long> current = new ArrayList<>(List.of(3L, 2L, 1L));

        when(cacheManager.getCache("feedIdTop")).thenReturn(topCache);
        when(topCache.get(cacheKey, List.class)).thenReturn(current);

        // when
        handler.updateTopIdsWithNewId(4L);

        // then
        verify(topCache).put(eq(cacheKey), argThat((List<Long> list) ->
                list.get(0).equals(4L) && list.size() == 4
        ));
    }

    @Test
    @DisplayName("evictFeedDetailIfPresent: 캐시에 존재하면 제거하고 true를 반환한다.")
    void evict_when_present() {
        when(cacheManager.getCache("feedDetail")).thenReturn(detailCache);
        when(detailCache.get(1L)).thenReturn(() -> new Object());

        boolean result = handler.evictFeedDetailIfPresent(1L);

        assertThat(result).isTrue();
        verify(detailCache).evict(1L);
    }

    @Test
    @DisplayName("evictFeedDetailIfPresent: 캐시에 없으면 false를 반환한다.")
    void evict_when_not_present() {
        when(cacheManager.getCache("feedDetail")).thenReturn(detailCache);
        when(detailCache.get(1L)).thenReturn(null);

        boolean result = handler.evictFeedDetailIfPresent(1L);

        assertThat(result).isFalse();
        verify(detailCache, never()).evict(any());
    }

    @Test
    @DisplayName("warmUpFeedDetails: DB 조회 후 캐시에 저장한다.")
    void warm_up_feed_details() {
        List<Long> ids = List.of(1L, 2L);

        FeedQueryDto dto1 = mock(FeedQueryDto.class);
        FeedQueryDto dto2 = mock(FeedQueryDto.class);

        when(dto1.feedId()).thenReturn(1L);
        when(dto2.feedId()).thenReturn(2L);

        when(cacheManager.getCache("feedDetail")).thenReturn(detailCache);
        when(feedJpaRepository.findFeedDetailsByIds(ids))
                .thenReturn(List.of(dto1, dto2));

        handler.warmUpFeedDetails(ids);

        verify(detailCache).put(1L, dto1);
        verify(detailCache).put(2L, dto2);
    }

    @Test
    @DisplayName("refreshCacheAfterBulkDelete: 삭제 대상이 topIds에 포함되면 인덱스 갱신한다.")
    void refresh_when_contains_deleted() {
        List<Long> currentTop = new ArrayList<>(List.of(5L, 4L, 3L));
        List<Long> deleted = List.of(5L);

        String cacheKey = "top" + DEFAULT_CACHE_SIZE;
        when(cacheManager.getCache("feedIdTop")).thenReturn(topCache);
        when(topCache.get(cacheKey, List.class))
                .thenReturn(currentTop);

        when(feedJpaRepository.findTopFeedIds(DEFAULT_CACHE_SIZE))
                .thenReturn(List.of(4L, 3L, 2L));

        handler.refreshCacheAfterBulkDelete(deleted);

        verify(topCache).put(cacheKey, List.of(4L, 3L, 2L));
    }
}
