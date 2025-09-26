package konkuk.thip.message.application.port.in;

import konkuk.thip.message.adapter.out.event.dto.FeedEvents;

public interface FeedNotificationDispatchUseCase {
    void handleFollower(FeedEvents.FollowerEvent e);

    void handleFeedCommented(FeedEvents.FeedCommentedEvent e);

    void handleFeedCommentReplied(FeedEvents.FeedCommentRepliedEvent e);

    void handleFolloweeNewFeed(FeedEvents.FolloweeNewFeedEvent e);

    void handleFeedLiked(FeedEvents.FeedLikedEvent e);

    void handleFeedCommentLiked(FeedEvents.FeedCommentLikedEvent e);
}