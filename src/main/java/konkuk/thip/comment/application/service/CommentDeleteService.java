package konkuk.thip.comment.application.service;

import konkuk.thip.comment.application.port.in.CommentDeleteUseCase;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.common.post.service.PostHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentDeleteService implements CommentDeleteUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentLikeCommandPort commentLikeCommandPort;

    private final PostHandler postHandler;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    @Override
    @Transactional
    public Long deleteComment(Long commentId, Long userId) {

        // 1. 댓글 조회 및 권한 검증
        Comment comment = commentCommandPort.getByIdOrThrow(commentId);
        // 1-1. 게시글 타입에 따른 댓글 삭제 권한 검증
        CountUpdatable post = postHandler.findPost(comment.getPostType(), comment.getTargetPostId());
        commentAuthorizationValidator.validateUserCanAccessPostForComment(comment.getPostType(), post, userId);

        // 2. 댓글 삭제 권한 검증 및 소프트 딜리트
        comment.validateDeletable(userId);
        // 2-1. 댓글 좋아요 삭제
        commentLikeCommandPort.deleteAllByCommentId(commentId);

        // 3. 댓글 삭제
        commentCommandPort.delete(comment);

        //TODO 게시물의 댓글 수 증가/감소 동시성 제어 로직 추가해야됨

        // 4. 게시글 댓글 수 감소
        // 4-1. 도메인 게시물 댓글 수 감소
        post.decreaseCommentCount();
        // 4-2 Jpa엔티티 게시물 댓글 수 감소
        postHandler.updatePost(comment.getPostType(), post);

        return post.getId();
    }

}
