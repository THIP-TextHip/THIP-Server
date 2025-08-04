package konkuk.thip.comment.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.comment.application.port.in.CommentLikeUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeCommand;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeResult;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.service.PostHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentLikeService implements CommentLikeUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentLikeCommandPort commentLikeCommandPort;

    private final PostHandler postHandler;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    @Override
    @Transactional
    public CommentIsLikeResult changeLikeStatusComment(CommentIsLikeCommand command) {

        // 1. 댓글 조회 및 검증 (존재 여부)
        Comment comment = commentCommandPort.getByIdOrThrow(command.commentId());
        // 1-1. 게시글 타입에 따른 댓글 좋아요 권한 검증
        CommentCountUpdatable post = postHandler.findPost(comment.getPostType(), comment.getTargetPostId());
        commentAuthorizationValidator.validateUserCanAccessPostForComment(comment.getPostType(), post, command.userId());

        // 2. 유저가 해당 댓글에 대해 좋아요 했는지 조회
        boolean alreadyLiked = commentLikeQueryPort.isLikedCommentByUser(command.userId(), command.commentId());

        // 3. 좋아요 상태변경
        if (command.isLike()) {
            comment.validateCanLike(alreadyLiked); // 좋아요 가능 여부 검증
            commentLikeCommandPort.save(command.userId(), command.commentId());
        } else {
            comment.validateCanUnlike(alreadyLiked); // 좋아요 취소 가능 여부 검증
            commentLikeCommandPort.delete(command.userId(), command.commentId());
        }

        // 5. 댓글 좋아요 수 업데이트
        comment.updateLikeCount(command.isLike());
        commentCommandPort.update(comment);

        return CommentIsLikeResult.of(comment.getId(), command.isLike());
    }
}
