package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.application.port.UserTokenBlacklistCommandPort;
import konkuk.thip.user.application.port.UserTokenBlacklistQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class UserTokenBlacklistRedisAdapter implements UserTokenBlacklistQueryPort, UserTokenBlacklistCommandPort {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.redis.token-blacklist-prefix}")
    private String blacklistPrefix;

    private final JwtUtil jwtUtil;

    /**
     * 블랙리스트에 토큰 추가 (토큰 만료시간에 맞춰 자동 소멸)
     */
    @Override
    public void addTokenToBlacklist(String token) {
        Date expiration = jwtUtil.getExpirationAllowExpired(token);
        String key = makeBlacklistKey(token);
        redisTemplate.opsForValue().set(key, "BLACKLISTED");
        // 토큰 만료시각으로 Redis에 expireAt 지정 (만료 이후 Redis에서 자동 삭제)
        redisTemplate.expireAt(key, expiration.toInstant());
    }

    /**
     * 토큰이 블랙리스트에 등록되어있는지 체크
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = makeBlacklistKey(token);
        return redisTemplate.hasKey(key);
    }

    /**
     * JWT 블랙리스트 Redis Key 생성 규칙
     */
    private String makeBlacklistKey(String token) {
        return blacklistPrefix + ":" + token;
    }

}
