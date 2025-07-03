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
    ;

    private final String value;

    AuthParameters(String value) {
        this.value = value;
    }
}

