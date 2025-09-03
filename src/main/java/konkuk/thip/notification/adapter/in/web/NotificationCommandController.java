package konkuk.thip.notification.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.notification.adapter.in.web.request.FcmTokenRequest;
import konkuk.thip.notification.application.port.in.FcmEnableStateChangeUseCase;
import konkuk.thip.notification.application.port.in.FcmRegisterUseCase;
import konkuk.thip.notification.adapter.in.web.request.FcmTokenEnableStateChangeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification Command API", description = "알림 관련 상태변경 API")
@RestController
@RequiredArgsConstructor
public class NotificationCommandController {

    private final FcmRegisterUseCase fcmRegisterUseCase;
    private final FcmEnableStateChangeUseCase fcmEnableStateChangeUseCase;

    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 서버에 등록합니다. 기존 토큰이 있다면 userId+deviceId 기준으로 토큰을 갱신합니다.")
    @PostMapping("/fcm-tokens")
    public BaseResponse<Void> registerFcmToken(
            @RequestBody @Valid FcmTokenRequest request,
            @Parameter(hidden = true) @UserId Long userId
            ) {
        fcmRegisterUseCase.registerToken(FcmTokenRequest.toCommand(request, userId));
        return BaseResponse.ok(null);
    }

    @Operation(summary = "푸시 알림 설정 변경", description = "사용자의 푸시 알림 수신 여부를 변경합니다.")
    @PatchMapping("/notifications/push")
    public BaseResponse<Void> updatePushNotificationSetting(
            @RequestBody FcmTokenEnableStateChangeRequest request,
            @Parameter(hidden = true) @UserId Long userId
    ) {
        fcmEnableStateChangeUseCase.changeEnableState();
        return BaseResponse.ok(null);
    }
}
