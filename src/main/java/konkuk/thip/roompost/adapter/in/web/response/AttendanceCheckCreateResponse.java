package konkuk.thip.roompost.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateResult;

import java.time.LocalDate;

public record AttendanceCheckCreateResponse(
        Long roomId,
        Long attendanceCheckId,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImageUrl,
        String todayComment,
        @Schema(description = "작성 시각(상대 시간 등 가공된 문자열)", example = "5분 전")
        String postDate,
        @Schema(description = "작성 날짜(yyyy-MM-dd)", example = "2025-08-17")
        LocalDate date,        // 해당 오늘의 한마디 데이터의 작성 날짜
        boolean isFirstWrite,
        boolean isWriter
) {
    public static AttendanceCheckCreateResponse of(AttendanceCheckCreateResult result) {
        boolean isFirstWrite = false;
        if (result.todayWriteCountOfUser() == 1) isFirstWrite = true;

        return new AttendanceCheckCreateResponse(
                result.roomId(),
                result.attendanceCheckId(),
                result.creatorId(),
                result.creatorNickname(),
                result.creatorProfileImageUrl(),
                result.todayComment(),
                result.postDate(),
                result.date(),
                isFirstWrite,
                result.isWriter()
        );
    }
}

