package konkuk.thip.notification.application.service;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.message.application.port.out.FeedEventCommandPort;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.application.service.template.feed.*;
import konkuk.thip.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@HelperService
@RequiredArgsConstructor
public class FeedNotificationOrchestratorSyncImpl implements FeedNotificationOrchestrator {

    /**
     * 정책:
     * 1) 알림(Notification) DB 저장은 비즈니스 트랜잭션과 동일한 경계 내에서 "동기"로 수행한다.
     *   -> 비즈니스 로직에서 시작한 상위 트랜잭션에 DB notification 저장이 포함되어야 하므로, Propagation.MANDATORY 강제
     * 2) 푸시 알림은 AFTER_COMMIT 리스너에서 "비동기"로 발송한다.
     */

    private final NotificationCommandPort notificationCommandPort;
    private final FeedEventCommandPort feedEventCommandPort;

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

    // ========================= Feed 영역 =========================
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFollowed(Long targetUserId, Long actorUserId, String actorUsername) {
        var args = new FollowedTemplate.Args(actorUsername);
        notifyWithTemplate(
                FollowedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> feedEventCommandPort.publishFollowEvent(targetUserId, actorUserId, actorUsername)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedCommented(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedCommentedTemplate.Args(actorUsername);
        notifyWithTemplate(
                FeedCommentedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> feedEventCommandPort.publishFeedCommentedEvent(targetUserId, actorUserId, actorUsername, feedId)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedReplied(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedRepliedTemplate.Args(actorUsername);
        notifyWithTemplate(
                FeedRepliedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> feedEventCommandPort.publishFeedRepliedEvent(targetUserId, actorUserId, actorUsername, feedId)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFolloweeNewPost(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FolloweeNewPostTemplate.Args(actorUsername);
        notifyWithTemplate(
                FolloweeNewPostTemplate.INSTANCE,
                args,
                targetUserId,
                () -> feedEventCommandPort.publishFolloweeNewPostEvent(targetUserId, actorUserId, actorUsername, feedId)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedLiked(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedLikedTemplate.Args(actorUsername);
        notifyWithTemplate(
                FeedLikedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> feedEventCommandPort.publishFeedLikedEvent(targetUserId, actorUserId, actorUsername, feedId)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedCommentLiked(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedCommentLikedTemplate.Args(actorUsername);
        notifyWithTemplate(
                FeedCommentLikedTemplate.INSTANCE,
                args,
                targetUserId,
                () -> feedEventCommandPort.publishFeedCommentLikedEvent(targetUserId, actorUserId, actorUsername, feedId)
        );
    }
}
