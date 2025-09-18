package konkuk.thip.notification.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_NOTIFICATION_TYPE;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    FEED("feed"),
    ROOM("room"),
    FEED_AND_ROOM("feedAndRoom");

    private final String type;

    public static NotificationType from(String type) {
        return Arrays.stream(NotificationType.values())
                .filter(param -> param.getType().equals(type))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(INVALID_NOTIFICATION_TYPE)
                );
    }
}
