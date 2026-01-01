package konkuk.thip.post.application.service;

import jakarta.persistence.LockTimeoutException;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.application.service.handler.PostHandler;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;
import konkuk.thip.post.application.port.in.dto.PostIsLikeResult;
import konkuk.thip.post.application.port.in.PostLikeUseCase;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.post.application.service.validator.PostLikeAuthorizationValidator;
import konkuk.thip.post.domain.service.PostCountService;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService implements PostLikeUseCase {

    private final PostLikeQueryPort postLikeQueryPort;
    private final PostLikeCommandPort postLikeCommandPort;
    private final UserCommandPort userCommandPort;

    private final PostHandler postHandler;
    private final PostCountService postCountService;
    private final PostLikeAuthorizationValidator postLikeAuthorizationValidator;

    private final FeedNotificationOrchestrator feedNotificationOrchestrator;
    private final RoomNotificationOrchestrator roomNotificationOrchestrator;

    @Override
    @Retryable(
            retryFor = {LockTimeoutException.class},
            noRetryFor = { BusinessException.class, InvalidStateException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2, maxDelay = 500, random = true)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PostIsLikeResult changeLikeStatusPost(PostIsLikeCommand command) {

        // 1. 게시물 타입에 맞게 검증 및 조회
        CountUpdatable post = postHandler.findPostForUpdate(command.postType(), command.postId());
        // 1-1. 게시글 타입에 따른 게시물 좋아요 권한 검증
        postLikeAuthorizationValidator.validateUserCanAccessPostLike(command.postType(), post, command.userId());

        // 2. 유저가 해당 게시물에 대해 좋아요 했는지 조회
        boolean alreadyLiked = postLikeQueryPort.isLikedPostByUser(command.userId(), command.postId());

        // 3. 게시물 좋아요 수 업데이트
        post.updateLikeCount(postCountService,command.isLike());
        postHandler.updatePost(command.postType(), post);

        // 4. 좋아요 상태변경
        if (command.isLike()) {
            postLikeAuthorizationValidator.validateUserCanLike(alreadyLiked); // 좋아요 가능 여부 검증
            postLikeCommandPort.save(command.userId(), command.postId(),command.postType());

            // 좋아요 푸쉬알림 전송
            sendNotifications(command);
        } else {
            postLikeAuthorizationValidator.validateUserCanUnLike(alreadyLiked); // 좋아요 취소 가능 여부 검증
            postLikeCommandPort.delete(command.userId(), command.postId());
        }

        return PostIsLikeResult.of(post.getId(), command.isLike());
    }

    @Recover
    public PostIsLikeResult recoverLockTimeout(LockTimeoutException e, PostIsLikeCommand command) {
        throw new BusinessException(ErrorCode.RESOURCE_LOCKED);
    }

    @Recover
    public PostIsLikeResult recoverInvalidStateException(InvalidStateException e, PostIsLikeCommand command) {
        throw e;
    }

    @Recover
    public PostIsLikeResult recoverBusinessException(BusinessException e, PostIsLikeCommand command) {
        throw e;
    }

    private void sendNotifications(PostIsLikeCommand command) {
        PostQueryDto postQueryDto = postHandler.getPostQueryDto(command.postType(), command.postId());

        if (command.userId().equals(postQueryDto.creatorId())) return; // 자신의 게시글에 좋아요 누르는 경우 제외

        User actorUser = userCommandPort.findById(command.userId());

        switch (command.postType()) {
            case FEED ->
                    feedNotificationOrchestrator.notifyFeedLiked(
                            postQueryDto.creatorId(), actorUser.getId(), actorUser.getNickname(), postQueryDto.postId()
                    );
            case RECORD, VOTE ->
                    roomNotificationOrchestrator.notifyRoomPostLiked(
                            postQueryDto.creatorId(), actorUser.getId(), actorUser.getNickname(), postQueryDto.roomId(), postQueryDto.page(), postQueryDto.postId(), command.postType()
                    );

        }
    }
}
