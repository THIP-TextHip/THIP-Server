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
    JWT_TOKEN_ATTRIBUTE("token"),
    REDIRECT_SIGNUP_URL("/signup"),
    REDIRECT_HOME_URL("/feed"),

    COOKIE_ACCESS_TOKEN("access_token"),
    COOKIE_TEMP_TOKEN("temp_token"),

    REDIRECT_URL_KEY("redirect_url"),
    REDIRECT_SESSION_KEY("oauth2_return_to"),

    ;

    private final String value;

    AuthParameters(String value) {
        this.value = value;
    }
}

