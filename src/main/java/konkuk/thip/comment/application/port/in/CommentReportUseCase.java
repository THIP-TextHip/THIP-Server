package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.application.port.in.dto.CommentReportResult;

public interface CommentReportUseCase {
    CommentReportResult reportComment(Long commentId);
}
