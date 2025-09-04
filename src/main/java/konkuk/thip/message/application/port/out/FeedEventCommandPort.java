package konkuk.thip.message.application.port.out;

public interface FeedEventCommandPort {

    // 누군가 나를 팔로우하는 경우
    void publishFollowEvent(Long targetUserId, Long actorUserId, String actorUsername);

    // 누군가 내 피드에 댓글을 다는 경우
    void publishFeedCommentedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                   Long feedId);

    // 누군가 내 댓글에 대댓글을 다는 경우
    void publishFeedRepliedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                 Long feedId);

    // 내가 팔로우하는 사람이 새 글을 올리는 경우
    void publishFolloweeNewPostEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                     Long feedId);

    // 내 피드가 좋아요를 받는 경우
    void publishFeedLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                               Long feedId);

    // 내 피드 댓글이 좋아요를 받는 경우
    void publishFeedCommentLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                      Long feedId);
}