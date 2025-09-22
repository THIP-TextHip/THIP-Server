package konkuk.thip.notification.adapter.in.web.response;

import konkuk.thip.notification.domain.value.MessageRoute;

import java.util.Map;

public record NotificationMarkToCheckedResponse(
        MessageRoute route,
        Map<String, Object> params
) {
}
