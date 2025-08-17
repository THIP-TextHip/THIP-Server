package konkuk.thip.common.security.oauth2.auth.dto;

import lombok.Builder;

@Builder
public record AuthSetCookieResponse(
        String type
) {
    public static AuthSetCookieResponse of(String type) {
        return new AuthSetCookieResponse(type);
    }
}