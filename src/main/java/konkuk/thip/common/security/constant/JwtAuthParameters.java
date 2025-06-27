package konkuk.thip.common.security.constant;

import lombok.Getter;

@Getter
public enum JwtAuthParameters {
    JWT_HEADER_KEY("Authorization"),
    JWT_PREFIX("Bearer "),

    ;

    private final String value;

    JwtAuthParameters(String value) {
        this.value = value;
    }
}

