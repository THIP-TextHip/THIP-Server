package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentQueryRepository {

    List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, String postTypeStr, LocalDateTime lastCreatedAt, int size, Long viewerId);

    List<CommentQueryDto> findAllActiveChildCommentsByCreatedAtAsc(Long rootCommentId, Long viewerId);

    Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsByCreatedAtAsc(Set<Long> rootCommentIds, Long viewerId);

    CommentQueryDto findRootCommentId(Long commentId);

    CommentQueryDto findChildCommentId(Long rootCommentId, Long commentId);
}
