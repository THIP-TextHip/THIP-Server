package konkuk.thip.comment.adapter.in.web.response;

import konkuk.thip.comment.application.port.in.dto.CommentReportResult;

public record CommentReportResponse(
        Long commentId,
        int reportCount
) {
        public static CommentReportResponse of(CommentReportResult commentReportResult) {
                return new CommentReportResponse(commentReportResult.commentId(), commentReportResult.reportCount());
        }
}
