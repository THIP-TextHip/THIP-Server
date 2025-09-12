package konkuk.thip.common.security.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.*;
import konkuk.thip.common.security.oauth2.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static konkuk.thip.common.security.constant.AuthParameters.JWT_ACCESS_TOKEN_KEY;
import static konkuk.thip.common.security.constant.AuthParameters.JWT_SIGNUP_TOKEN_KEY;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    //todo 확정 후 환경변수로 변경
    private final long tokenExpiredMs = 2592000000L; // 30일
    private final long signupTokenExpiredMs = 2592000000L; // 30일

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createSignupToken(String oauth2Id) {
        return Jwts.builder()
                .claim(JWT_SIGNUP_TOKEN_KEY.getValue(), oauth2Id)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + signupTokenExpiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createAccessToken(Long userId) {
        return Jwts.builder()
                .claim(JWT_ACCESS_TOKEN_KEY.getValue(), userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpiredMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT Signature", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    private String getOauth2Id(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JWT_SIGNUP_TOKEN_KEY.getValue(), String.class);
    }

    private Long getUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get(JWT_ACCESS_TOKEN_KEY.getValue(), Long.class);
    }

    public LoginUser getLoginUser(String token) {
        String oauth2Id = getOauth2Id(token);
        Long userId = getUserId(token);

        if (userId == null) {
            return LoginUser.createNewUser(oauth2Id);
        }
        return LoginUser.createExistingUser(oauth2Id, userId);
    }

    public Date getExpirationAllowExpired(String token) {
        try {
            Jws<Claims> jwt = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return jwt.getPayload().getExpiration();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getExpiration();
        }
    }

}
