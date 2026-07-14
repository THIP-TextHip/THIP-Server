package konkuk.thip.config.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheType {
    // 최신순 피드 ID 100개 인덱스 캐시
    FEED_ID_TOP(
            "feedIdTop",
            0,  // 만료 시간 없음
            1), //
    // 개별 피드 상세 정보를 담는 데이터 캐시
    FEED_DETAIL(
            "feedDetail",
                    0,    // 만료 시간 없음
                    100);

    private final String cacheName;
    private final int expireAfterWrite;
    private final int maximumSize;
}
