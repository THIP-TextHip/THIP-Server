package konkuk.thip.comment.application.service;

import konkuk.thip.comment.adapter.in.web.response.CommentCreateResponse;
import konkuk.thip.comment.application.mapper.CommentQueryMapper;
import konkuk.thip.comment.application.port.in.RootCommentCreateUseCase;
import konkuk.thip.comment.application.port.in.dto.RootCommentCreateCommand;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.comment.application.service.validator.CommentAuthorizationValidator;
import konkuk.thip.comment.domain.Comment;
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

@Service
@RequiredArgsConstructor
public class RootCommentCreateService implements RootCommentCreateUseCase {

    private final CommentCommandPort commentCommandPort;
    private final CommentQueryPort commentQueryPort;
    private final CommentQueryMapper commentQueryMapper;
    private final UserCommandPort userCommandPort;

    private final PostHandler postHandler;
    private final CommentAuthorizationValidator commentAuthorizationValidator;

    private final FeedNotificationOrchestrator feedNotificationOrchestrator;
    private final RoomNotificationOrchestrator roomNotificationOrchestrator;

    @Override
    @Transactional
    public CommentCreateResponse createRootComment(RootCommentCreateCommand command) {
        PostType type = PostType.from(command.postType());

        // 1. 게시물 조회 및 댓글 생성 권한 검증
        CountUpdatable post = postHandler.findPost(type, command.postId());
        commentAuthorizationValidator.validateUserCanAccessPostForComment(type, post, command.userId());

        // 2. 게시글 작성자에게 알림 전송
        PostQueryDto postQueryDto = postHandler.getPostQueryDto(type, post.getId());
        User actorUser = userCommandPort.findById(command.userId());
        sendNotificationsToPostWriter(postQueryDto, actorUser);

        // 3. 루트 댓글 생성
        Comment comment = Comment.createRootComment(
                command.content(), command.postId(), command.userId(), type
        );
        Long savedCommentId = commentCommandPort.save(comment);

        // 4. 게시글 댓글 수 증가
        post.increaseCommentCount();
        postHandler.updatePost(type, post);

        // 5. 응답 매핑
        CommentQueryDto savedCommentDto = commentQueryPort.findRootCommentById(savedCommentId);
        return commentQueryMapper.toRoot(savedCommentDto, false, command.userId());
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
}
