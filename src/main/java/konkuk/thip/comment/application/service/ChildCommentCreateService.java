package konkuk.thip.comment.application.service;

import konkuk.thip.comment.adapter.in.web.response.CommentCreateResponse;
import konkuk.thip.comment.application.mapper.CommentQueryMapper;
import konkuk.thip.comment.application.port.in.ChildCommentCreateUseCase;
import konkuk.thip.comment.application.port.in.dto.ChildCommentCreateCommand;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.application.service.handler.PostHandler;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_COMMENT_CREATE;

@Service
@RequiredArgsConstructor
public class ChildCommentCreateService implements ChildCommentCreateUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentQueryPort commentQueryPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentQueryMapper commentQueryMapper;
    private final UserCommandPort userCommandPort;

    private final PostHandler postHandler;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    private final FeedNotificationOrchestrator feedNotificationOrchestrator;
    private final RoomNotificationOrchestrator roomNotificationOrchestrator;

    @Override
    @Transactional
    public CommentCreateResponse createChildComment(ChildCommentCreateCommand command) {
        PostType type = PostType.from(command.postType());

        // 1. 부모 댓글 조회 및 존재 검증
        Comment parentComment = commentCommandPort.findById(command.parentCommentId())
                .orElseThrow(() -> new InvalidStateException(
                        INVALID_COMMENT_CREATE,
                        new IllegalArgumentException("parentId에 해당하는 부모 댓글이 존재해야 합니다.")
                ));

        // 2. 게시물 조회 및 댓글 생성 권한 검증
        CountUpdatable post = postHandler.findPost(type, command.postId());
        commentAuthorizationValidator.validateUserCanAccessPostForComment(type, post, command.userId());

        // 3. 알림 전송 (게시글 작성자에게)
        PostQueryDto postQueryDto = postHandler.getPostQueryDto(type, post.getId());
        User actorUser = userCommandPort.findById(command.userId());
        sendNotificationsToPostWriter(postQueryDto, actorUser);

        // 4. 자식 댓글 생성
        Comment comment = Comment.createChildComment(
                command.content(), command.postId(), command.userId(), parentComment, type
        );
        Long savedCommentId = commentCommandPort.save(comment);

        // 5. 게시글 댓글 수 증가
        post.increaseCommentCount();
        postHandler.updatePost(type, post);

        // 6. 응답 매핑 (부모 댓글 + 생성된 답글)
        CommentQueryDto parentCommentDto = commentQueryPort.findRootCommentById(command.parentCommentId());

        // 부모 댓글 작성자에게 알림 전송
        sendNotificationsToParentCommentWriter(postQueryDto, parentCommentDto, actorUser);

        // 부모 댓글 좋아요 여부 조회
        boolean isLikedParentComment = commentLikeQueryPort.isLikedCommentByUser(command.userId(), parentCommentDto.commentId());

        CommentQueryDto savedReplyCommentDto = commentQueryPort.findChildCommentById(command.parentCommentId(), savedCommentId);
        return commentQueryMapper.toRootCommentResponseWithChildren(parentCommentDto, savedReplyCommentDto, isLikedParentComment, command.userId());
    }

    private void sendNotificationsToPostWriter(PostQueryDto postQueryDto, User actorUser) {
        if (postQueryDto.creatorId().equals(actorUser.getId())) return;

        PostType postType = PostType.from(postQueryDto.postType());
        switch (postType) {
            case FEED ->
                    feedNotificationOrchestrator.notifyFeedCommented(
                            postQueryDto.creatorId(), actorUser.getId(), actorUser.getNickname(), postQueryDto.postId()
                    );
            case RECORD, VOTE ->
                    roomNotificationOrchestrator.notifyRoomPostCommented(
                            postQueryDto.creatorId(), actorUser.getId(), actorUser.getNickname(),
                            postQueryDto.roomId(), postQueryDto.page(), postQueryDto.postId(), postType
                    );
        }
    }

    private void sendNotificationsToParentCommentWriter(PostQueryDto postQueryDto, CommentQueryDto parentCommentDto, User actorUser) {
        if (parentCommentDto.creatorId().equals(actorUser.getId())) return;

        PostType postType = PostType.from(postQueryDto.postType());
        switch (postType) {
            case FEED ->
                    feedNotificationOrchestrator.notifyFeedReplied(
                            parentCommentDto.creatorId(), actorUser.getId(), actorUser.getNickname(), postQueryDto.postId()
                    );
            case RECORD, VOTE ->
                    roomNotificationOrchestrator.notifyRoomPostCommentReplied(
                            parentCommentDto.creatorId(), actorUser.getId(), actorUser.getNickname(),
                            postQueryDto.roomId(), postQueryDto.page(), postQueryDto.postId(), postType
                    );
        }
    }
}
