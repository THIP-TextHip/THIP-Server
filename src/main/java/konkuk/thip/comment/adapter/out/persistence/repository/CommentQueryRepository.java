package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentQueryRepository {

    List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, Long lastRootCommentId, int size);

    List<CommentQueryDto> findAllActiveChildCommentsByCreatedAtAsc(Long rootCommentId);

    Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsByCreatedAtAsc(Set<Long> rootCommentIds);

    List<CommentQueryDto> findChildCommentsByCreatedAtAsc(Long rootCommentId, Long lastChildCommentId, int size);

    CommentQueryDto findRootCommentId(Long commentId);

    CommentQueryDto findChildCommentId(Long rootCommentId, Long commentId);
}
