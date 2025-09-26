package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 읽음 처리 요청 DTO")
public record NotificationMarkToCheckedRequest(

        @NotNull
        @Schema(description = "읽음 처리할 알림 ID", example = "1")
        Long notificationId
) { }
