package konkuk.thip.roompost.adapter.in.web.response;

import java.time.LocalDate;
import java.util.List;

public record AttendanceCheckShowResponse(
        List<AttendanceCheckShowDto> todayCommentList,
        String nextCursor,
        boolean isLast
) {
    public record AttendanceCheckShowDto(
            Long attendanceCheckId,
            Long creatorId,
            String creatorNickname,
            String creatorProfileImageUrl,
            String todayComment,
            String postDate,
            LocalDate date,        // 해당 오늘의 한마디 데이터의 작성 날짜
            boolean isWriter
    ) { }
}
