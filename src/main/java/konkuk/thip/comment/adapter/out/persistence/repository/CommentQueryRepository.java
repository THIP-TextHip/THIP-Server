package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentQueryRepository {

    List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, Long userId, String postTypeStr, LocalDateTime lastCreatedAt, int size);

    List<CommentQueryDto> findAllActiveChildCommentsByCreatedAtAsc(Long rootCommentId,Long userId);

    Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsByCreatedAtAsc(Set<Long> rootCommentIds,Long userId);
}
