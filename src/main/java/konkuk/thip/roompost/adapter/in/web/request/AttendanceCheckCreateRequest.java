package konkuk.thip.roompost.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateCommand;

public record AttendanceCheckCreateRequest(
        @Schema(description = "유저가 작성한 오늘의 한마디 내용")
        @NotBlank(message = "오늘의 한마디 내용은 필수입니다.")
        String content
) {
    public AttendanceCheckCreateCommand toCommand(Long creatorId, Long roomId) {
        return new AttendanceCheckCreateCommand(creatorId, roomId, content);
    }
}
