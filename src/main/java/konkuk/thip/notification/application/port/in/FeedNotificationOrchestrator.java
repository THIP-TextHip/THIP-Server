package konkuk.thip.notification.application.port.in;

public interface FeedNotificationOrchestrator {

    /**
     * 비즈니스 로직 이후, NotificationOrchestrator 를 호출하여 알림 관련 로직 실행
     * -> DB에 notification data save + 푸시알림
     */

    // ===== Feed 영역 =====
    void notifyFollowed(Long targetUserId, Long actorUserId, String actorUsername);

    void notifyFeedCommented(Long targetUserId, Long actorUserId, String actorUsername, Long feedId);

    void notifyFeedReplied(Long targetUserId, Long actorUserId, String actorUsername, Long feedId);

    void notifyFolloweeNewFeed(Long targetUserId, Long actorUserId, String actorUsername, Long feedId);

    void notifyFeedLiked(Long targetUserId, Long actorUserId, String actorUsername, Long feedId);

    void notifyFeedCommentLiked(Long targetUserId, Long actorUserId, String actorUsername, Long feedId);
}
