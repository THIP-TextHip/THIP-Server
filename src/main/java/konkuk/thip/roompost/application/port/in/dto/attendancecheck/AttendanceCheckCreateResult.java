package konkuk.thip.roompost.application.port.in.dto.attendancecheck;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AttendanceCheckCreateResult(
        Long roomId,
        Long attendanceCheckId,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImageUrl,
        String todayComment,
        String postDate,
        LocalDate date,        // 해당 오늘의 한마디 데이터의 작성 날짜
        int todayWriteCountOfUser,
        boolean isWriter
) { }
