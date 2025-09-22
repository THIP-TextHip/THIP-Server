package konkuk.thip.notification.domain.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {
    FEED("피드"),
    ROOM("모임");

    private final String display;

    public String prefixedTitle(String title) {
        return "[" + display + "] " + title;
    }
}