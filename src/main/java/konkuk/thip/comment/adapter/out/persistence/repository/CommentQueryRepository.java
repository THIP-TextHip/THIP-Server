package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentQueryRepository {

    List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, LocalDateTime lastCreatedAt, int size);

    List<CommentQueryDto> findAllActiveChildrenCommentsByCreatedAtAsc(Long rootCommentId);
}
