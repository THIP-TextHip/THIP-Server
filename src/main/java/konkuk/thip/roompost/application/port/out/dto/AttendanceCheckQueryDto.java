package konkuk.thip.roompost.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record AttendanceCheckQueryDto(
        Long attendanceCheckId,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImageUrl,
        String todayComment,
        LocalDateTime createdAt
) {
    @QueryProjection
    public AttendanceCheckQueryDto {}
}
