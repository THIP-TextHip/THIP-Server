package konkuk.thip.common.security.oauth2.apple;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AppleRefreshTokenStore {

    private static final String PREFIX = "apple:refresh:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String oauth2Id, String refreshToken) {
        redisTemplate.opsForValue().set(PREFIX + oauth2Id, refreshToken, TTL);
    }

    public String pop(String oauth2Id) {
        String key = PREFIX + oauth2Id;
        String token = redisTemplate.opsForValue().get(key);
        if (token != null) {
            redisTemplate.delete(key);
        }
        return token;
    }
}
