// message/adapter/out/event/dto/FeedEvents.java
package konkuk.thip.message.adapter.out.event.dto;

import lombok.Builder;

public class FeedEvents {

    // 누군가 나를 팔로우하는 경우
    @Builder
    public record FollowerEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername) {}

    // 누군가 내 피드에 댓글을 다는 경우
    @Builder
    public record FeedCommentedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long feedId) {}

    // 누군가 내 댓글에 대댓글을 다는 경우
    @Builder
    public record FeedCommentRepliedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long feedId) {}

    // 내가 팔로우하는 사람이 새 글을 올리는 경우
    @Builder
    public record FolloweeNewPostEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long feedId) {}

    // 내 피드가 좋아요를 받는 경우
    @Builder
    public record FeedLikedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long feedId) {}

    // 내 피드 댓글이 좋아요를 받는 경우
    @Builder
    public record FeedCommentLikedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long feedId) {}
}
