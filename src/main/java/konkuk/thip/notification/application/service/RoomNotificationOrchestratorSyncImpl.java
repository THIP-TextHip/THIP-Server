package konkuk.thip.notification.application.service;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.application.service.template.room.*;
import konkuk.thip.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@HelperService
@RequiredArgsConstructor
public class RoomNotificationOrchestratorSyncImpl implements RoomNotificationOrchestrator {

    /**
     * 정책:
     * 1) 알림(Notification) DB 저장은 비즈니스 트랜잭션과 동일한 경계 내에서 "동기"로 수행한다.
     *   -> 비즈니스 로직에서 시작한 상위 트랜잭션에 DB notification 저장이 포함되어야 하므로, Propagation.MANDATORY 강제
     * 2) 푸시 알림은 AFTER_COMMIT 리스너에서 "비동기"로 발송한다.
     */

    private final NotificationCommandPort notificationCommandPort;
    private final RoomEventCommandPort roomEventCommandPort;

    // ========================= 공통 헬퍼 =========================
    private <T> void notifyWithTemplate(
            NotificationTemplate<T> template,
            T args,
            Long targetUserId,
            Runnable eventPublisher
    ) {
        String title = template.title(args);
        String content = template.content(args);
        saveNotification(title, content, targetUserId);
        eventPublisher.run();
    }

    private void saveNotification(String title, String content, Long targetUserId) {
        Notification notification = Notification.withoutId(title, content, targetUserId);
        notificationCommandPort.save(notification);
    }

    // ========================= Room 영역 =========================
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomPostCommented(Long targetUserId, Long actorUserId, String actorUsername,
                                        Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomPostCommentedTemplate.Args(actorUsername);
        notifyWithTemplate(
                RoomPostCommentedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomPostCommentedEvent(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomVoteStarted(Long targetUserId, Long roomId, String roomTitle, Integer page, Long postId) {
        var args = new RoomVoteStartedTemplate.Args(roomTitle);
        notifyWithTemplate(
                RoomVoteStartedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomVoteStartedEvent(targetUserId, roomId, roomTitle, page, postId)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomRecordCreated(Long targetUserId, Long actorUserId, String actorUsername,
                                        Long roomId, String roomTitle, Integer page, Long postId) {
        var args = new RoomRecordCreatedTemplate.Args(roomTitle, actorUsername);
        notifyWithTemplate(
                RoomRecordCreatedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomRecordCreatedEvent(targetUserId, actorUserId, actorUsername, roomId, roomTitle, page, postId)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomRecruitClosedEarly(Long targetUserId, Long roomId, String roomTitle) {
        var args = new RoomRecruitClosedEarlyTemplate.Args(roomTitle);
        notifyWithTemplate(
                RoomRecruitClosedEarlyTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomRecruitClosedEarlyEvent(targetUserId, roomId, roomTitle)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomActivityStarted(Long targetUserId, Long roomId, String roomTitle) {
        var args = new RoomActivityStartedTemplate.Args(roomTitle);
        notifyWithTemplate(
                RoomActivityStartedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomActivityStartedEvent(targetUserId, roomId, roomTitle)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomJoinToHost(Long hostUserId, Long roomId, String roomTitle, Long actorUserId, String actorUsername) {
        var args = new RoomJoinToHostTemplate.Args(roomTitle, actorUsername);
        notifyWithTemplate(
                RoomJoinToHostTemplate.INSTANCE,
                args,
                hostUserId,
                () -> roomEventCommandPort.publishRoomJoinEventToHost(hostUserId, roomId, roomTitle, actorUserId, actorUsername)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomCommentLiked(Long targetUserId, Long actorUserId, String actorUsername,
                                       Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomCommentLikedTemplate.Args(actorUsername);
        notifyWithTemplate(
                RoomCommentLikedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomCommentLikedEvent(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomPostLiked(Long targetUserId, Long actorUserId, String actorUsername,
                                    Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomPostLikedTemplate.Args(actorUsername);
        notifyWithTemplate(
                RoomPostLikedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomPostLikedEvent(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyRoomPostCommentReplied(Long targetUserId, Long actorUserId, String actorUsername,
                                             Long roomId, Integer page, Long postId, String postType) {
        var args = new RoomPostCommentRepliedTemplate.Args(actorUsername);
        notifyWithTemplate(
                RoomPostCommentRepliedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> roomEventCommandPort.publishRoomPostCommentRepliedEvent(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType)
        );
    }
}
