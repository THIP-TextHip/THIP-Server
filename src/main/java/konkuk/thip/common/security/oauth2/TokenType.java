package konkuk.thip.common.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {

    TEMP("TEMP"), ACCESS("ACCESS");

    private final String value;
}
