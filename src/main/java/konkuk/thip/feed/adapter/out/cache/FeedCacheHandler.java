package konkuk.thip.feed.adapter.out.cache;

import java.util.ArrayList;
import java.util.List;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCacheHandler {

    private static final int DEFAULT_CACHE_SIZE = 100;

    private final FeedJpaRepository feedJpaRepository;
    private final CacheManager cacheManager;

    @Cacheable(cacheNames = "feedIdTop", key = "'top' + " +  DEFAULT_CACHE_SIZE)
    public List<Long> getTopIds() {
        return feedJpaRepository.findTopFeedIds(DEFAULT_CACHE_SIZE);
    }

    @Cacheable(cacheNames = "feedDetail", key = "#feedId")
    public FeedQueryDto getFeedDetail(Long feedId) {
        return feedJpaRepository.findFeedDetailById(feedId);
    }

    @CachePut(cacheNames = "feedDetail", key = "#feedId")
    public FeedQueryDto markAsDeleted(Long feedId) {
        return null;
    }

    public void updateTopIdsWithNewId(Long newId) {
        Cache cache = cacheManager.getCache("feedIdTop");
        if (cache == null) return;

        String cacheKey = "top" + DEFAULT_CACHE_SIZE;
        List<Long> currentIds = cache.get(cacheKey, List.class);

        if (currentIds == null) {
            currentIds = feedJpaRepository.findTopFeedIds(DEFAULT_CACHE_SIZE);
        }

        List<Long> updatedIds = new ArrayList<>(currentIds);
        updatedIds.add(0, newId); // 최신 피드를 맨 앞으로

        if (updatedIds.size() > DEFAULT_CACHE_SIZE) {
            updatedIds.remove(DEFAULT_CACHE_SIZE);
        }

        cache.put(cacheKey, updatedIds);
    }

    public boolean evictFeedDetailIfPresent(Long feedId) {
        Cache cache = cacheManager.getCache("feedDetail");
        if (cache == null) return false;

        Cache.ValueWrapper valueWrapper = cache.get(feedId);
        if (valueWrapper != null) {
            cache.evict(feedId);
            return true;
        }
        return false;
    }

    public void warmUpFeedDetails(List<Long> feedIds) {
        Cache detailCache = cacheManager.getCache("feedDetail");
        if (detailCache == null || feedIds.isEmpty()) return;

        List<FeedQueryDto> details = feedJpaRepository.findFeedDetailsByIds(feedIds);

        for (FeedQueryDto dto : details) {
            detailCache.put(dto.feedId(), dto);
        }
    }

    public void refreshCacheAfterBulkDelete(List<Long> deletedFeedIds) {
        Cache cache = cacheManager.getCache("feedIdTop");
        if (cache == null) return;

        // 1. 현재 캐시된 인덱스 확보
        String cacheKey = "top" + DEFAULT_CACHE_SIZE;
        List<Long> currentIds = cache.get(cacheKey, List.class);
        if (currentIds == null) {
            return;
        }
        // 2. 포함 여부 확인
        boolean hasTopFeed = deletedFeedIds.stream().anyMatch(currentIds::contains);

        if (hasTopFeed) {
            // 3. 기존 리스트에서 삭제 대상 제거
            List<Long> oldIdsWithoutDeleted = new ArrayList<>(currentIds);
            oldIdsWithoutDeleted.removeAll(deletedFeedIds);

            // 4. 상세 캐시에서 탈퇴자 피드 제거
            evictFeeds(deletedFeedIds);

            // 5. 인덱스 강제 갱신
            List<Long> newTopIds = feedJpaRepository.findTopFeedIds(DEFAULT_CACHE_SIZE);
            cache.put(cacheKey, newTopIds);

            // 6. 신규 진입 ID 추출
            List<Long> newlyAddedIds = new ArrayList<>(newTopIds);
            newlyAddedIds.removeAll(oldIdsWithoutDeleted);

            // 7. 신규 진입 피드들만 정밀 워밍업
            warmUpFeedDetails(newlyAddedIds);
        }
    }

    private void evictFeeds(List<Long> feedIds) {
        Cache cache = cacheManager.getCache("feedDetail");
        if (cache != null && feedIds != null) {
            feedIds.forEach(cache::evict);
        }
    }

}