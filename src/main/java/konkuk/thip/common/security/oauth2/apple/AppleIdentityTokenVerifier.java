package konkuk.thip.common.security.oauth2.apple;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import konkuk.thip.common.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;

import static konkuk.thip.common.exception.code.ErrorCode.AUTH_APPLE_IDENTITY_TOKEN_INVALID;

@Slf4j
@Component
public class AppleIdentityTokenVerifier {

    private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public AppleIdentityTokenVerifier() {
        try {
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(APPLE_JWK_SET_URI));
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource));
        } catch (Exception e) {
            throw new IllegalStateException("Apple JWK 초기화 실패", e);
        }
    }

    public String verify(String identityToken) {
        try {
            JWTClaimsSet claims = jwtProcessor.process(identityToken, null);
            String sub = claims.getSubject();
            log.info("[Apple Login] verified sub={}", sub);
            return sub;
        } catch (Exception e) {
            log.warn("[Apple Login] identity token verification failed: {}", e.getMessage());
            throw new AuthException(AUTH_APPLE_IDENTITY_TOKEN_INVALID);
        }
    }
}
