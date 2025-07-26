package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.application.port.in.dto.CommentCreateCommand;

public interface CommentCreateUseCase {
    Long createComment(CommentCreateCommand command);
}
