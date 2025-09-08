package konkuk.thip.user.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import konkuk.thip.common.exception.ExternalApiException;
import konkuk.thip.common.security.oauth2.LoginUser;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.application.port.UserTokenBlacklistCommandPort;
import konkuk.thip.user.application.port.UserTokenBlacklistQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.JSON_PROCESSING_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserTokenBlacklistRedisAdapter implements UserTokenBlacklistQueryPort, UserTokenBlacklistCommandPort {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.redis.token-blacklist-prefix}")
    private String blacklistPrefix;

    private final JwtUtil jwtUtil;

    //블랙리스트에 토큰 추가 (토큰 만료시간에 맞춰 자동 소멸)
    @Override
    public void addTokenToBlacklist(String token) {
        Date expiration = jwtUtil.getExpirationAllowExpired(token);
        LoginUser loginUser = jwtUtil.getLoginUser(token);
        LocalDateTime withdrawalTime = LocalDateTime.now();
        String key = makeBlacklistKey(token);

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("userId", loginUser.userId());
        valueMap.put("withdrawalTime", withdrawalTime);
        String valueJson = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            valueJson = mapper.writeValueAsString(valueMap);
        } catch (JsonProcessingException e) {
            throw new ExternalApiException(JSON_PROCESSING_ERROR);
        }
        redisTemplate.opsForValue().set(key, valueJson);
        log.info("Add token to blacklist - userId: {}, withdrawalTime: {}, expiration: {}",
                loginUser.userId(),
                withdrawalTime,
                expiration
        );
        // 토큰 만료시각으로 Redis에 expireAt 지정 (만료 이후 Redis에서 자동 삭제)
        redisTemplate.expireAt(key, expiration.toInstant());
    }

    // 토큰이 블랙리스트에 등록되어있는지 체크
    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = makeBlacklistKey(token);
        return redisTemplate.hasKey(key);
    }

    // JWT 블랙리스트 Redis Key 생성 규칙
    private String makeBlacklistKey(String token) {
        return blacklistPrefix + ":" + token;
    }

}
