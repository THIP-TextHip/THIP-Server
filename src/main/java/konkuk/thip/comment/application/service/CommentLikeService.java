package konkuk.thip.comment.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.comment.application.port.in.CommentLikeUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeCommand;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeResult;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.comment.domain.LikedComments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentLikeService implements CommentLikeUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentLikeCommandPort commentLikeCommandPort;

    @Override
    @Transactional
    public CommentIsLikeResult changeLikeStatusComment(CommentIsLikeCommand command) {

        // 1. 댓글 조회 및 검증 (존재 여부)
        Comment comment = commentCommandPort.getByIdOrThrow(command.commentId());

        // 2. 유저가 좋아요 한 댓글 목록 조회
        LikedComments likedComments = commentLikeQueryPort.findLikedCommentsByUserId(command.userId());

        // 3. 좋아요 상태변경
        if (command.isLike()) {
            // 좋아요 요청 시 이미 좋아요되어 있으면 예외 발생
            likedComments.validateNotAlreadyLiked(comment);
            commentLikeCommandPort.save(command.userId(),comment.getId());
        } else {
            // 좋아요 취소 요청 시 좋아요되어 있지 않으면 예외 발생
            likedComments.validateCanUnlike(comment);
            commentLikeCommandPort.delete(command.userId(),comment.getId());
        }

        // 4. 댓글 좋아요 수 업데이트
        comment.updateLikeCount(command.isLike());
        commentCommandPort.update(comment);

        return CommentIsLikeResult.of(comment.getId(), command.isLike());
    }
}
