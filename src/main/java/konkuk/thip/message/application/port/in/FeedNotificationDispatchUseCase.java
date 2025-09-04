package konkuk.thip.message.application.port.in;

import konkuk.thip.message.adapter.out.event.dto.FeedEvents;

public interface FeedNotificationDispatchUseCase {
    void handleFollower(FeedEvents.FollowerEvent e);

    void handleFeedCommented(FeedEvents.FeedCommentedEvent e);

    void handleFeedReplied(FeedEvents.FeedRepliedEvent e);

    void handleFolloweeNewPost(FeedEvents.FolloweeNewPostEvent e);

    void handleFeedLiked(FeedEvents.FeedLikedEvent e);

    void handleFeedCommentLiked(FeedEvents.FeedCommentLikedEvent e);
}