package konkuk.thip.common.security.oauth2.apple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleTokenClient {

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";

    private final RestTemplate restTemplate;
    private final AppleProperties appleProperties;
    private final AppleClientSecretGenerator clientSecretGenerator;

    public String exchangeAuthorizationCode(String authorizationCode) {
        String clientSecret = clientSecretGenerator.generate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", appleProperties.getClientId());
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            Map<?, ?> response = restTemplate.postForObject(
                    APPLE_TOKEN_URL,
                    new HttpEntity<>(params, headers),
                    Map.class
            );
            if (response == null || !response.containsKey("refresh_token")) {
                log.warn("[Apple] authorizationCode 교환 실패: refresh_token 없음");
                return null;
            }
            return (String) response.get("refresh_token");
        } catch (Exception e) {
            log.warn("[Apple] authorizationCode 교환 중 오류: {}", e.getMessage());
            return null;
        }
    }

    public void revokeToken(String refreshToken) {
        String clientSecret = clientSecretGenerator.generate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", appleProperties.getClientId());
        params.add("client_secret", clientSecret);
        params.add("token", refreshToken);
        params.add("token_type_hint", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            restTemplate.postForObject(APPLE_REVOKE_URL, new HttpEntity<>(params, headers), Void.class);
            log.info("[Apple] 토큰 철회 완료");
        } catch (Exception e) {
            log.warn("[Apple] 토큰 철회 중 오류: {}", e.getMessage());
        }
    }
}
