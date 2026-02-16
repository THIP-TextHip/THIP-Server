package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.adapter.in.web.response.CommentCreateResponse;
import konkuk.thip.comment.application.port.in.dto.ChildCommentCreateCommand;

public interface ChildCommentCreateUseCase {

    CommentCreateResponse createChildComment(ChildCommentCreateCommand command);
}
