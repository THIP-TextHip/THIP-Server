package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;

import java.util.List;

public interface CommentQueryRepository {

    List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, Long lastRootCommentId, int size);

    List<CommentQueryDto> findAllDescendantCommentsByCreatedAtAsc(Long rootCommentId, Long lastCommentId, int size);

    CommentQueryDto findRootCommentId(Long commentId);

    CommentQueryDto findChildCommentId(Long rootCommentId, Long commentId);
}
