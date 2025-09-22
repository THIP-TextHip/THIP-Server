package konkuk.thip.notification.domain.value;

import java.util.Map;

public record NotificationRedirectSpec(
        MessageRoute route, // FE 이동 목적지
        Map<String, Object> params  // 목적지로 이동 시 필요한 파라미터들
) {
    public static NotificationRedirectSpec none() {
        return new NotificationRedirectSpec(MessageRoute.NONE, Map.of());
    }
}
