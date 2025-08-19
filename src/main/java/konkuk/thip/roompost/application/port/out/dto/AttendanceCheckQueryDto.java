package konkuk.thip.roompost.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.user.domain.Alias;

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
    public AttendanceCheckQueryDto (
            Long attendanceCheckId,
            Long creatorId,
            String creatorNickname,
            Alias alias,
            String todayComment,
            LocalDateTime createdAt
    ) {
        this.attendanceCheckId = attendanceCheckId;
        this.creatorId = creatorId;
        this.creatorNickname = creatorNickname;
        this.creatorProfileImageUrl = alias.getImageUrl();
        this.todayComment = todayComment;
        this.createdAt = createdAt;
    }
}
