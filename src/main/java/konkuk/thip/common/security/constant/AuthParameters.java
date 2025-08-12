package konkuk.thip.common.security.constant;

import lombok.Getter;

@Getter
public enum AuthParameters {
    JWT_HEADER_KEY("Authorization"),
    JWT_PREFIX("Bearer "),
    KAKAO("kakao"),
    GOOGLE("google"),
    KAKAO_PROVIDER_ID_KEY("id"),
    GOOGLE_PROVIDER_ID_KEY("sub"),
    JWT_ACCESS_TOKEN_KEY("userId"),
    JWT_SIGNUP_TOKEN_KEY("oauth2Id"),
    REDIRECT_SIGNUP_URL("/signup"),
    REDIRECT_HOME_URL("/feed"),
    HTTPS_PREFIX("https://"),
    HTTP_PREFIX("http://")
    ;

    private final String value;

    AuthParameters(String value) {
        this.value = value;
    }
}

