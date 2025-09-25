package konkuk.thip.notification.application.port.in;

import konkuk.thip.post.domain.PostType;

public interface RoomNotificationOrchestrator {

    /**
     * 비즈니스 로직 이후, NotificationOrchestrator 를 호출하여 알림 관련 로직 실행
     * -> DB에 notification data save + 푸시알림
     */

    // ===== Room 영역 =====
    void notifyRoomPostCommented(Long targetUserId, Long actorUserId, String actorUsername,
                                 Long roomId, Integer page, Long postId, PostType postType);

    void notifyRoomVoteStarted(Long targetUserId, Long roomId, String roomTitle, Integer page, Long postId);

    void notifyRoomRecordCreated(Long targetUserId, Long actorUserId, String actorUsername,
                                 Long roomId, String roomTitle, Integer page, Long postId);

    void notifyRoomRecruitClosedEarly(Long targetUserId, Long roomId, String roomTitle);

    void notifyRoomActivityStarted(Long targetUserId, Long roomId, String roomTitle);

    void notifyRoomJoinToHost(Long hostUserId, Long roomId, String roomTitle,
                              Long actorUserId, String actorUsername);

    void notifyRoomCommentLiked(Long targetUserId, Long actorUserId, String actorUsername,
                                Long roomId, Integer page, Long postId, PostType postType);

    void notifyRoomPostLiked(Long targetUserId, Long actorUserId, String actorUsername,
                             Long roomId, Integer page, Long postId, PostType postType);

    void notifyRoomPostCommentReplied(Long targetUserId, Long actorUserId, String actorUsername,
                                      Long roomId, Integer page, Long postId, PostType postType);
}
