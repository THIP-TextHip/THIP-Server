package konkuk.thip.comment.application.port.out;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;

import java.util.List;

public interface CommentQueryPort {

    CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, Cursor cursor);

    List<CommentQueryDto> findAllActiveChildrenComments(Long rootCommentId);
}
