package konkuk.thip.post.application.service;

import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.post.application.port.out.PostLikeEventCommandPort;
import konkuk.thip.post.application.port.out.PostLikeRedisQueryPort;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.application.service.handler.PostHandler;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;
import konkuk.thip.post.application.port.in.dto.PostIsLikeResult;
import konkuk.thip.post.application.port.in.PostLikeUseCase;
import konkuk.thip.post.application.service.validator.PostLikeAuthorizationValidator;
import konkuk.thip.post.domain.service.PostCountService;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService implements PostLikeUseCase {

    private final UserCommandPort userCommandPort;
    private final PostLikeRedisQueryPort postLikeRedisQueryPort;

    private final PostHandler postHandler;
    private final PostCountService postCountService;
    private final PostLikeAuthorizationValidator postLikeAuthorizationValidator;

    private final FeedNotificationOrchestrator feedNotificationOrchestrator;
    private final RoomNotificationOrchestrator roomNotificationOrchestrator;
    private final PostLikeEventCommandPort postLikeEventCommandPort;

    @Override
    @Transactional
    public PostIsLikeResult changeLikeStatusPost(PostIsLikeCommand command) {

        // 1. 게시물 타입에 맞게 검증 및 조회
        CountUpdatable post = postHandler.findPost(command.postType(), command.postId());
        // 1-1. 게시글 타입에 따른 게시물 좋아요 권한 검증
        postLikeAuthorizationValidator.validateUserCanAccessPostLike(command.postType(), post, command.userId());

        // 2. 유저가 해당 게시물에 대해 좋아요 했는지 조회
        boolean alreadyLiked = postLikeRedisQueryPort.isLikedPostByUser(command.userId(), command.postId());

        // 3. 좋아요 가능 여부 검증
        if (command.isLike()) {
            postLikeAuthorizationValidator.validateUserCanLike(alreadyLiked);
            // 좋아요 푸쉬알림 전송
            //sendNotifications(command);
        } else {
            postLikeAuthorizationValidator.validateUserCanUnLike(alreadyLiked);
        }

        // 4. 도메인 상태 갱신 (게시물 좋아요 수)
        int redisLikeCount = postLikeRedisQueryPort.getLikeCount(command.postType(), post.getId(), post.getLikeCount());
        post.updateLikeCount(postCountService, command.isLike(), redisLikeCount); // 도메인 상태 갱신 (외부에서 읽은 최신값 주입)

        // 5. 트랜잭션 후속 처리 (DB 커밋 성공 후 비동기 작업을 위한 이벤트 발행)
        postLikeEventCommandPort.publishEvent(command.userId(), command.postId(), command.isLike(),
                command.postType(), post.getLikeCount());
        return PostIsLikeResult.of(post.getId(), command.isLike());
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
