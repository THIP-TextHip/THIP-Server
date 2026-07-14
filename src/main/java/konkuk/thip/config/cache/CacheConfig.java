package konkuk.thip.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                .map(cacheType -> new CaffeineCache(
                        cacheType.getCacheName(),
                        caffeineBuilder(cacheType)
                ))
                .collect(Collectors.toList());

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    private Cache<Object, Object> caffeineBuilder(CacheType cacheType) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .recordStats()
                .maximumSize(cacheType.getMaximumSize());

        // 0초 무한유지 설정
        if (cacheType.getExpireAfterWrite() > 0) {
            builder.expireAfterWrite(cacheType.getExpireAfterWrite(), TimeUnit.SECONDS);
        }

        return builder.build();
    }
}