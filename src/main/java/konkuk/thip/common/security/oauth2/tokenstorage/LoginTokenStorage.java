package konkuk.thip.common.security.oauth2.tokenstorage;

import konkuk.thip.common.security.oauth2.TokenType;

import java.time.Duration;

public interface LoginTokenStorage {

    void put(String key, TokenType type, String token, Duration ttl);

    /**
     * 저장된 토큰을 1회용으로 소비 후 삭제한다.
     * 존재하지 않으면 null 반환.
     */
    Entry consume(String key);

    record Entry(TokenType type, String token) {
    }
}