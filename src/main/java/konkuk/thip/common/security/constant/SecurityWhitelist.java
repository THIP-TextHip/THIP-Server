package konkuk.thip.common.security.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum SecurityWhitelist {

    SWAGGER_UI("/swagger-ui/**"),
    API_DOCS("/api-docs/**"),
    SWAGGER_UI_HTML("/swagger-ui.html"),
    V3_API_DOCS("/v3/api-docs/**"),
    OAUTH2_AUTHORIZATION("/oauth2/authorization/**"),
    LOGIN_OAUTH2_CODE("/login/oauth2/code/**"),
    ACTUATOR_HEALTH("/actuator/health"),
    AUTH_USERS("/auth/users"),
    AUTH_TOKEN("/auth/token"),
    API_TEST("/api/test/**"),
    AUTH_EXCHANGE_TEMP_TOKEN("/auth/exchange-temp-token"),
    AUTH_SET_COOKIE("/auth/set-cookie");

    private final String pattern;

    // SecurityConfig 용 전체 리스트
    public static String[] patterns() {
        return Arrays.stream(values())
                .map(SecurityWhitelist::getPattern)
                .toArray(String[]::new);
    }

    // JwtAuthenticationFilter.shouldNotFilter() 용 편의 메서드
    public static List<String> patternsList() {
        return Arrays.asList(patterns());
    }
}
