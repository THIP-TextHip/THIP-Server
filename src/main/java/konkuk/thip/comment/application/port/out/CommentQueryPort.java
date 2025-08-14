package konkuk.thip.comment.application.port.out;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentQueryPort {

    CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, Long userId, String postTypeStr, Cursor cursor);

    List<CommentQueryDto> findAllActiveChildCommentsOldestFirst(Long rootCommentId,Long userId);

    Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsOldestFirst(Set<Long> rootCommentIds,Long userId);
}
