package konkuk.thip.common.security.oauth2.tokenstorage;

import konkuk.thip.common.security.oauth2.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Profile("test")
@Component
@RequiredArgsConstructor
public class MemoryLoginTokenStorage implements LoginTokenStorage{
    private record InternalEntry(TokenType type, String token, long expireAtEpochMillis) { }

    private final ConcurrentHashMap<String, InternalEntry> store = new ConcurrentHashMap<>();

    /**
     * 토큰을 메모리에 저장 (TTL 적용)
     */
    @Override
    public void put(String key, TokenType type, String token, Duration ttl) {
        long expiredAt = Instant.now().plus(ttl).toEpochMilli();
        store.put(key, new InternalEntry(type, token, expiredAt));
    }

    /**
     * 토큰을 일회성으로 조회 후 제거 (만료 시 null 반환)
     */
    @Override
    public Entry consume(String key) {
        InternalEntry entry = store.remove(key);
        if (entry == null) return null;

        if (entry.expireAtEpochMillis() < Instant.now().toEpochMilli()) {
            return null; // 만료
        }

        // 외부에는 최소 DTO만 반환 (내부 정보 캡슐화)
        return new Entry(entry.type(), entry.token());
    }
}
