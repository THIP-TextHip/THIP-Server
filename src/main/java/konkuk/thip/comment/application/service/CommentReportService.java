package konkuk.thip.comment.application.service;

import konkuk.thip.comment.application.port.in.CommentReportUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentReportResult;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentReportService implements CommentReportUseCase {

    private final CommentCommandPort commentCommandPort;

    @Override
    @Transactional
    public CommentReportResult reportComment(Long commentId) {
        Comment comment = commentCommandPort.getByIdOrThrow(commentId);
        comment.increaseReportCount();
        commentCommandPort.update(comment);

        return CommentReportResult.of(comment.getId(), comment.getReportCount());
    }
}
