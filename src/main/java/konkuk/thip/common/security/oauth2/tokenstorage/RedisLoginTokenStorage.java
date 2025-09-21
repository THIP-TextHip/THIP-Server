package konkuk.thip.common.security.oauth2.tokenstorage;

import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.security.oauth2.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Profile({"!test"})
@Component
@RequiredArgsConstructor
public class RedisLoginTokenStorage implements LoginTokenStorage {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "auth:login-token:";

    @Override
    public void put(String key, TokenType type, String token, Duration ttl) {
        String redisKey = toRedisKey(key);
        Entry entry = new Entry(type, token);

        redisTemplate.opsForValue().set(redisKey, entry, ttl);
    }

    @Override
    public Entry consume(String key) {
        String redisKey = toRedisKey(key);
        Object value = redisTemplate.opsForValue().getAndDelete(redisKey);
        if (value == null) {
            return null;
        }

        if (value instanceof Entry entry) {
            return entry;
        }

        throw new AuthException(ErrorCode.JSON_PROCESSING_ERROR);
    }

    private String toRedisKey(String key) {
        return PREFIX + key;
    }
}