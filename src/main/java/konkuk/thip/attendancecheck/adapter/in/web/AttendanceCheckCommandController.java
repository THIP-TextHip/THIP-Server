package konkuk.thip.attendancecheck.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import konkuk.thip.attendancecheck.adapter.in.web.request.AttendanceCheckCreateRequest;
import konkuk.thip.attendancecheck.adapter.in.web.response.AttendanceCheckCreateResponse;
import konkuk.thip.attendancecheck.application.port.in.AttendanceCheckCreateUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.feed.adapter.in.web.response.FeedIdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.ATTENDANCE_CHECK_CREATE;

@RestController
@RequiredArgsConstructor
public class AttendanceCheckCommandController {

    private final AttendanceCheckCreateUseCase attendanceCheckCreateUseCase;

    @Operation(
            summary = "오늘의 한마디 작성",
            description = "방 참여자가 오늘의 한마디를 작성합니다."
    )
    @ExceptionDescription(ATTENDANCE_CHECK_CREATE)
    @PostMapping("/rooms/{roomId}/daily-greeting")
    public BaseResponse<AttendanceCheckCreateResponse> createFeed(
            @RequestBody @Valid final AttendanceCheckCreateRequest request,
            @Parameter(description = "오늘의 한마디를 작성할 방 id값") @PathVariable("roomId") final Long roomId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(AttendanceCheckCreateResponse.of(
                attendanceCheckCreateUseCase.create(request.toCommand(userId, roomId))
        ));
    }
}
