package konkuk.thip.common.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class LoginTokenStorage {

    @Getter
    public static final class Entry {
        private final TokenType type;
        private final String token;
        private final long expireAtEpochMillis;

        private Entry(TokenType type, String token, long expireAtEpochMillis) {
            this.type = type;
            this.token = token;
            this.expireAtEpochMillis = expireAtEpochMillis;
        }
    }

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    /**
     * 토큰을 메모리에 저장 (TTL 적용)
     */
    public void put(String key, TokenType type, String token, Duration ttl) {
        long expiredAt = Instant.now().plus(ttl).toEpochMilli();
        store.put(key, new Entry(type, token, expiredAt));
    }

    /**
     * 토큰을 일회성으로 조회 후 제거 (만료 시 null 반환)
     */
    public Entry consume(String key) {
        Entry entry = store.remove(key);

        if (entry == null) return null;
        if (entry.expireAtEpochMillis < Instant.now().toEpochMilli()) return null;
        return entry;
    }
}
