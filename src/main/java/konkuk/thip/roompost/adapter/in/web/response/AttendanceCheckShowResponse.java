package konkuk.thip.roompost.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(
        description = "오늘의 한마디 조회 응답. 작성 시각 기준으로 최신순으로 정렬하여 응답합니다."
)
public record AttendanceCheckShowResponse(
        @Schema(
                description = "오늘의 한마디 목록, 10개씩 끊어서 응답합니다."
        )
        List<AttendanceCheckShowDto> todayCommentList,

        @Schema(
                description = "다음 페이지 조회를 위한 커서(없으면 null)"
        )
        String nextCursor,

        boolean isLast
) {
    public record AttendanceCheckShowDto(
            Long attendanceCheckId,
            Long creatorId,
            String creatorNickname,
            String creatorProfileImageUrl,
            String todayComment,

            @Schema(description = "작성 시각(상대 시간 등 가공된 문자열)", example = "5분 전")
            String postDate,

            @Schema(description = "작성 날짜(yyyy-MM-dd), 이걸로 날짜별로 끊어서 화면에 보여주시면 됩니다.", example = "2025-08-17")
            LocalDate date,        // 해당 오늘의 한마디 데이터의 작성 날짜

            @Schema(description = "현재 사용자가 해당 글을 작성했는지 여부")
            boolean isWriter
    ) { }
}
