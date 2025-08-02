package konkuk.thip.comment.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.comment.application.port.in.CommentDeleteUseCase;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentDeleteService implements CommentDeleteUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentLikeCommandPort commentLikeCommandPort;

    private final PostQueryService postQueryService;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {

        // 1. 댓글 조회 및 권한 검증
        Comment comment = commentCommandPort.getByIdOrThrow(commentId);
        // 1-1. 게시글 타입에 따른 댓글 삭제 권한 검증
        CommentCountUpdatable post = postQueryService.findPost(comment.getPostType(), comment.getTargetPostId());
        commentAuthorizationValidator.validateUserCanAccessPostForComment(comment.getPostType(), post, userId);

        // 2. 댓글 Soft Delete 처리
        comment.softDelete(userId);
        commentCommandPort.update(comment);

        //TODO 게시물의 댓글 수 증가/감소 동시성 제어 로직 추가해야됨

        // 3. 게시글 댓글 수 감소
        // 3-1. 도메인 게시물 댓글 수 감소
        post.decreaseCommentCount();
        // 3-2 Jpa엔티티 게시물 댓글 수 감소
        postQueryService.updatePost(comment.getPostType(), post);

        // 4. 댓글 좋아요 삭제
        commentLikeCommandPort.deleteAllByCommentId(commentId);
    }

}
