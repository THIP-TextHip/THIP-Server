package konkuk.thip.comment.application.service;

import konkuk.thip.comment.application.port.in.CommentLikeUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeCommand;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeResult;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.message.application.port.out.FeedEventCommandPort;
import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.application.service.handler.PostHandler;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService implements CommentLikeUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentLikeCommandPort commentLikeCommandPort;
    private final UserCommandPort userCommandPort;

    private final PostHandler postHandler;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    private final FeedEventCommandPort feedEventCommandPort;
    private final RoomEventCommandPort roomEventCommandPort;

    @Override
    @Transactional
    public CommentIsLikeResult changeLikeStatusComment(CommentIsLikeCommand command) {

        // 1. 댓글 조회 및 검증 (존재 여부)
        Comment comment = commentCommandPort.getByIdOrThrow(command.commentId());
        // 1-1. 게시글 타입에 따른 댓글 좋아요 권한 검증
        CountUpdatable post = postHandler.findPost(comment.getPostType(), comment.getTargetPostId());
        commentAuthorizationValidator.validateUserCanAccessPostForComment(comment.getPostType(), post, command.userId());

        // 2. 유저가 해당 댓글에 대해 좋아요 했는지 조회
        boolean alreadyLiked = commentLikeQueryPort.isLikedCommentByUser(command.userId(), command.commentId());

        // 3. 좋아요 상태변경
        if (command.isLike()) {
            comment.validateCanLike(alreadyLiked); // 좋아요 가능 여부 검증
            commentLikeCommandPort.save(command.userId(), command.commentId());

            // 댓글 좋아요 푸쉬알림 전송
            sendNotifications(command, comment);
        } else {
            comment.validateCanUnlike(alreadyLiked); // 좋아요 취소 가능 여부 검증
            commentLikeCommandPort.delete(command.userId(), command.commentId());
        }

        // 5. 댓글 좋아요 수 업데이트
        comment.updateLikeCount(command.isLike());
        commentCommandPort.update(comment);

        return CommentIsLikeResult.of(comment.getId(), command.isLike());
    }

    private void sendNotifications(CommentIsLikeCommand command, Comment comment) {
        if (command.userId().equals(comment.getCreatorId())) return; // 자신의 댓글에 좋아요 누르는 경우 제외

        User actorUser = userCommandPort.findById(command.userId());
        // 좋아요 푸쉬알림 전송
        if (comment.getPostType() == PostType.FEED) {
            feedEventCommandPort.publishFeedCommentLikedEvent(comment.getCreatorId(), actorUser.getId(), actorUser.getNickname(), comment.getTargetPostId());
        }
        if (comment.getPostType() == PostType.RECORD || comment.getPostType() == PostType.VOTE) {
            PostQueryDto postQueryDto = postHandler.getPostQueryDto(comment.getPostType(), comment.getTargetPostId());
            roomEventCommandPort.publishRoomCommentLikedEvent(comment.getCreatorId(), actorUser.getId(), actorUser.getNickname(), postQueryDto.roomId(), postQueryDto.page(), postQueryDto.postId());
        }
    }
}
