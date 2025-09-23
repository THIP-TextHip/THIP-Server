package konkuk.thip.notification.application.service;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.notification.application.service.template.room.*;
import konkuk.thip.notification.domain.value.MessageRoute;
import konkuk.thip.notification.domain.value.NotificationRedirectSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@HelperService
@RequiredArgsConstructor
public class RoomNotificationOrchestratorSyncImpl implements RoomNotificationOrchestrator {

    /**
     * 정책:
     * 1) 알림(Notification) DB 저장은 비즈니스 트랜잭션과 동일한 경계 내에서 "동기"로 수행한다.
     *   -> 비즈니스 로직에서 시작한 상위 트랜잭션에 DB notification 저장이 포함되어야 하므로, Propagation.MANDATORY 강제
     * 2) 푸시 알림은 AFTER_COMMIT 리스너에서 "비동기"로 발송한다.
     */

    private final NotificationSyncExecutor notificationSyncExecutor;
    private final RoomEventCommandPort roomEventCommandPort;

    // ========================= Room 영역 =========================
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomPostCommented(Long targetUserId, Long actorUserId, String actorUsername,
                                        Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomPostCommentedTemplate.Args(actorUsername);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_POST_DETAIL,
                Map.of(
                        "roomId", roomId,
                        "page", page,
                        "postId", postId,
                        "postType", postType
                )
        );

        notificationSyncExecutor.execute(
                RoomPostCommentedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomPostCommentedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomVoteStarted(Long targetUserId, Long roomId, String roomTitle, Integer page, Long postId) {
        var args = new RoomVoteStartedTemplate.Args(roomTitle);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_VOTE_DETAIL,
                Map.of(
                        "roomId", roomId,
                        "page", page,
                        "postId", postId,
                        "postType", "VOTE"
                )
        );

        notificationSyncExecutor.execute(
                RoomVoteStartedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomVoteStartedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomRecordCreated(Long targetUserId, Long actorUserId, String actorUsername,
                                        Long roomId, String roomTitle, Integer page, Long postId) {
        var args = new RoomRecordCreatedTemplate.Args(roomTitle, actorUsername);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_RECORD_DETAIL,
                Map.of(
                        "roomId", roomId,
                        "page", page,
                        "postId", postId,
                        "postType", "RECORD"
                )
        );

        notificationSyncExecutor.execute(
                RoomRecordCreatedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomRecordCreatedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomRecruitClosedEarly(Long targetUserId, Long roomId, String roomTitle) {
        var args = new RoomRecruitClosedEarlyTemplate.Args(roomTitle);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_MAIN,
                Map.of("roomId", roomId)
        );

        notificationSyncExecutor.execute(
                RoomRecruitClosedEarlyTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomRecruitClosedEarlyEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomActivityStarted(Long targetUserId, Long roomId, String roomTitle) {
        var args = new RoomActivityStartedTemplate.Args(roomTitle);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_MAIN,
                Map.of("roomId", roomId)
        );

        notificationSyncExecutor.execute(
                RoomActivityStartedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomActivityStartedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomJoinToHost(Long hostUserId, Long roomId, String roomTitle, Long actorUserId, String actorUsername) {
        var args = new RoomJoinToHostTemplate.Args(roomTitle, actorUsername);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_DETAIL,
                Map.of("roomId", roomId)
        );

        notificationSyncExecutor.execute(
                RoomJoinToHostTemplate.INSTANCE,
                args,
                hostUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomJoinEventToHost(
                        title, content, notificationId, hostUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomCommentLiked(Long targetUserId, Long actorUserId, String actorUsername,
                                       Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomCommentLikedTemplate.Args(actorUsername);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_POST_DETAIL,
                Map.of(
                        "roomId", roomId,
                        "page", page,
                        "postId", postId,
                        "postType", postType
                )
        );

        notificationSyncExecutor.execute(
                RoomCommentLikedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomCommentLikedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomPostLiked(Long targetUserId, Long actorUserId, String actorUsername,
                                    Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomPostLikedTemplate.Args(actorUsername);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_POST_DETAIL,
                Map.of(
                        "roomId", roomId,
                        "page", page,
                        "postId", postId,
                        "postType", postType
                )
        );

        notificationSyncExecutor.execute(
                RoomPostLikedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomPostLikedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomPostCommentReplied(Long targetUserId, Long actorUserId, String actorUsername,
                                             Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomPostCommentRepliedTemplate.Args(actorUsername);

        NotificationRedirectSpec redirectSpec = new NotificationRedirectSpec(
                MessageRoute.ROOM_POST_DETAIL,
                Map.of(
                        "roomId", roomId,
                        "page", page,
                        "postId", postId,
                        "postType", postType
                )
        );

        notificationSyncExecutor.execute(
                RoomPostCommentRepliedTemplate.INSTANCE,
                args,
                targetUserId,
                redirectSpec,
                (title, content, notificationId) -> roomEventCommandPort.publishRoomPostCommentRepliedEvent(
                        title, content, notificationId, targetUserId
                )
        );
    }
}
