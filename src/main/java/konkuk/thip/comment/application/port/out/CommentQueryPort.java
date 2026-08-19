package konkuk.thip.comment.application.port.out;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentQueryPort {

    CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, String postTypeStr, Cursor cursor, Long viewerId);

    List<CommentQueryDto> findAllActiveChildCommentsOldestFirst(Long rootCommentId, Long viewerId);

    Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsOldestFirst(Set<Long> rootCommentIds, Long viewerId);

    CommentQueryDto findRootCommentById(Long rootCommentId);

    CommentQueryDto findChildCommentById(Long rootCommentId , Long replyCommentId);
}
