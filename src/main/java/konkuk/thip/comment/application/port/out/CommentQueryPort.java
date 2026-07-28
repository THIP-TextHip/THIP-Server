package konkuk.thip.comment.application.port.out;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;

public interface CommentQueryPort {

    CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, Cursor cursor);

    CursorBasedList<CommentQueryDto> findAllDescendantComments(Long rootCommentId, Cursor cursor);

    CommentQueryDto findRootCommentById(Long rootCommentId);

    CommentQueryDto findChildCommentById(Long rootCommentId , Long replyCommentId);
}
