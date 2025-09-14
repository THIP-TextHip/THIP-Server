package konkuk.thip.notification.application.service;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.message.application.port.out.FeedEventCommandPort;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.notification.application.service.template.feed.*;
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

    private final NotificationSyncExecutor notificationSyncExecutor;
    private final FeedEventCommandPort feedEventCommandPort;

    // ========================= Feed 영역 =========================
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFollowed(Long targetUserId, Long actorUserId, String actorUsername) {
        var args = new FollowedTemplate.Args(actorUsername);
        notificationSyncExecutor.execute(
                FollowedTemplate.INSTANCE,
                args,
                targetUserId,
                (title, content) -> feedEventCommandPort.publishFollowEvent(
                        title, content, targetUserId, actorUserId, actorUsername
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedCommented(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedCommentedTemplate.Args(actorUsername);
        notificationSyncExecutor.execute(
                FeedCommentedTemplate.INSTANCE,
                args,
                targetUserId,
                (title, content) -> feedEventCommandPort.publishFeedCommentedEvent(
                        title, content, targetUserId, actorUserId, actorUsername, feedId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedReplied(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedRepliedTemplate.Args(actorUsername);
        notificationSyncExecutor.execute(
                FeedRepliedTemplate.INSTANCE,
                args,
                targetUserId,
                (title, content) -> feedEventCommandPort.publishFeedRepliedEvent(
                        title, content, targetUserId, actorUserId, actorUsername, feedId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFolloweeNewPost(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FolloweeNewPostTemplate.Args(actorUsername);
        notificationSyncExecutor.execute(
                FolloweeNewPostTemplate.INSTANCE,
                args,
                targetUserId,
                (title, content) -> feedEventCommandPort.publishFolloweeNewPostEvent(
                        title, content, targetUserId, actorUserId, actorUsername, feedId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedLiked(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedLikedTemplate.Args(actorUsername);
        notificationSyncExecutor.execute(
                FeedLikedTemplate.INSTANCE,
                args,
                targetUserId,
                (title, content) -> feedEventCommandPort.publishFeedLikedEvent(
                        title, content, targetUserId, actorUserId, actorUsername, feedId
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyFeedCommentLiked(Long targetUserId, Long actorUserId, String actorUsername, Long feedId) {
        var args = new FeedCommentLikedTemplate.Args(actorUsername);
        notificationSyncExecutor.execute(
                FeedCommentLikedTemplate.INSTANCE,
                args,
                targetUserId,
                (title, content) -> feedEventCommandPort.publishFeedCommentLikedEvent(
                        title, content, targetUserId, actorUserId, actorUsername, feedId
                )
        );
    }
}
