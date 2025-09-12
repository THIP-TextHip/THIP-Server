package konkuk.thip.notification.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.notification.adapter.in.web.response.NotificationShowEnableStateResponse;
import konkuk.thip.notification.application.port.in.NotificationShowEnableStateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.NOTIFICATION_GET_ENABLE_STATE;

@Tag(name = "Notification Query API", description = "알림 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class NotificationQueryController {

    private final NotificationShowEnableStateUseCase notificationShowEnableStateUseCase;

    @Operation(
            summary = "사용자 푸시알림 수신여부 조회 (마이페이지 -> 알림설정)",
            description = "알림설정 페이지에서 사용자의 푸시알림 수신여부 정보를 조회합니다."
    )
    @ExceptionDescription(NOTIFICATION_GET_ENABLE_STATE)
    @GetMapping("/users/notification-settings")
    public BaseResponse<NotificationShowEnableStateResponse> showNotificationEnableState(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "디바이스 고유 ID", example = "device12345")
            @RequestParam("deviceId") final String deviceId) {
        return BaseResponse.ok(
                NotificationShowEnableStateResponse.of(notificationShowEnableStateUseCase.getNotificationShowEnableState(userId,deviceId)));
    }
}
