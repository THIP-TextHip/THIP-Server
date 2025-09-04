package konkuk.thip.message.application.port.out;

public interface RoomEventCommandPort {

    // 내 모임방 기록/투표에 댓글이 달린 경우
    void publishRoomPostCommentedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                       Long roomId, Integer page, Long postId, String postType);

    // 내가 참여한 모임방에 새로운 투표가 시작된 경우
    void publishRoomVoteStartedEvent(Long targetUserId, Long roomId, String roomTitle,
                                     Integer page, Long postId);

    // 내가 참여한 모임방에 새로운 기록이 작성된 경우
    void publishRoomRecordCreatedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                       Long roomId, String roomTitle, Integer page, Long postId);

    // 내가 참여한 모임방이 조기 종료된 경우 (호스트가 모집 마감 버튼 누른 경우)
    void publishRoomRecruitClosedEarlyEvent(Long targetUserId, Long roomId, String roomTitle);

    // 내가 참여한 모임방 활동이 시작된 경우 (방이 시작 기간이 되어 자동으로 시작된 경우)
    void publishRoomActivityStartedEvent(Long targetUserId, Long roomId, String roomTitle);

    // 내가 방장일 때, 새로운 사용자가 모임방 참여를 한 경우
    void publishRoomJoinEventToHost(Long ownerUserId, Long roomId, String roomTitle,
                                    Long applicantUserId, String applicantUsername);

    // 내가 참여한 모임방의 나의 댓글이 좋아요를 받는 경우
    void publishRoomCommentLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                      Long roomId, Integer page, Long postId);

    // 내가 참여한 모임방 안의 나의 기록/투표가 좋아요를 받는 경우
    void publishRoomPostLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                   Long roomId, Integer page, Long postId, String postType);

    // 내가 참여한 모임방의 나의 댓글에 대댓글이 달린 경우
    void publicRoomPostCommentRepliedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                   Long roomId, Integer page, Long postId, String postType);
}