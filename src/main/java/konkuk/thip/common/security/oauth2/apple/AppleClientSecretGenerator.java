package konkuk.thip.common.security.oauth2.apple;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AppleClientSecretGenerator {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24 * 180; // 180일

    private final AppleProperties appleProperties;

    public String generate() {
        try {
            ECPrivateKey privateKey = loadPrivateKey(appleProperties.getPrivateKey());

            return Jwts.builder()
                    .header().add("kid", appleProperties.getKeyId()).and()
                    .issuer(appleProperties.getTeamId())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .audience().add(APPLE_AUDIENCE).and()
                    .subject(appleProperties.getClientId())
                    .signWith(privateKey, Jwts.SIG.ES256)
                    .compact();
        } catch (Exception e) {
            throw new IllegalStateException("Apple client_secret 생성 실패", e);
        }
    }

    private ECPrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        String stripped = privateKeyStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(stripped);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(spec);
    }
}
