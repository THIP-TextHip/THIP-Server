package konkuk.thip.comment.application.port.in.dto;

public record CommentReportResult(
        Long commentId,
        int reportCount
) {
    public static CommentReportResult of(Long commentId, int reportCount) {
        return new CommentReportResult(commentId, reportCount);
    }
}
