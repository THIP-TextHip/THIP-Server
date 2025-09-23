package konkuk.thip.notification.domain.value;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_FE_PLATFORM;

@Getter
@RequiredArgsConstructor
public enum PlatformType {
    ANDROID("ANDROID"),
    WEB("WEB");

    private final String value;

    public static PlatformType from(String value) {
        for (PlatformType type : PlatformType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new InvalidStateException(INVALID_FE_PLATFORM);
    }
}
