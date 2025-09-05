package konkuk.thip.notification.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.notification.adapter.in.web.request.FcmTokenDeleteRequest;
import konkuk.thip.notification.adapter.in.web.request.FcmTokenRegisterRequest;
import konkuk.thip.notification.adapter.in.web.response.FcmTokenEnableStateChangeResponse;
import konkuk.thip.notification.application.port.in.FcmEnableStateChangeUseCase;
import konkuk.thip.notification.application.port.in.FcmRegisterUseCase;
import konkuk.thip.notification.adapter.in.web.request.FcmTokenEnableStateChangeRequest;
import konkuk.thip.notification.application.port.in.FcmDeleteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Notification Command API", description = "알림 관련 상태변경 API")
@RestController
@RequiredArgsConstructor
public class NotificationCommandController {

    private final FcmRegisterUseCase fcmRegisterUseCase;
    private final FcmEnableStateChangeUseCase fcmEnableStateChangeUseCase;
    private final FcmDeleteUseCase fcmDeleteUseCase;

    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 서버에 등록합니다. 기존 토큰이 있다면 deviceId 기준으로 토큰을 갱신합니다.")
    @PostMapping("/fcm-tokens")
    @ExceptionDescription(FCM_TOKEN_REGISTER)
    public BaseResponse<Void> registerFcmToken(
            @RequestBody @Valid FcmTokenRegisterRequest request,
            @Parameter(hidden = true) @UserId Long userId
            ) {
        fcmRegisterUseCase.registerToken(request.toCommand(userId));
        return BaseResponse.ok(null);
    }

    @Operation(summary = "푸시 알림 수신 여부 설정 변경", description = "사용자의 푸시 알림 수신 여부를 변경합니다.")
    @ExceptionDescription(FCM_TOKEN_ENABLE_STATE_CHANGE)
    @PatchMapping("/notifications/enable-state")
    public BaseResponse<FcmTokenEnableStateChangeResponse> updatePushNotificationSetting(
            @RequestBody @Valid FcmTokenEnableStateChangeRequest request,
            @Parameter(hidden = true) @UserId Long userId
    ) {
        return BaseResponse.ok(
                FcmTokenEnableStateChangeResponse.of(fcmEnableStateChangeUseCase.changeEnableState(request.toCommand(userId))));
    }

    @DeleteMapping("/fcm-tokens")
    @Operation(summary = "FCM 토큰 삭제", description = "사용자의 FCM 토큰을 삭제합니다.")
    public BaseResponse<Void> deleteFcmToken(
            @RequestBody @Valid FcmTokenDeleteRequest request,
            @Parameter(hidden = true) @UserId Long userId
    ) {
        fcmDeleteUseCase.deleteToken(request.toCommand(userId));
        return BaseResponse.ok(null);
    }
}
