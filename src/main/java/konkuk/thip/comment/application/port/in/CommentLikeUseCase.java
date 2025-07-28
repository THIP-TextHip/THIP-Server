package konkuk.thip.comment.application.port.in;

import konkuk.thip.comment.application.port.in.dto.CommentIsLikeCommand;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeResult;

public interface CommentLikeUseCase {
    CommentIsLikeResult changeLikeStatusComment(CommentIsLikeCommand commentIsLikeCommand);
}
