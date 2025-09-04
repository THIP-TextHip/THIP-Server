// notification/adapter/in/event/NotificationEventHandler.java (발췌: Room 관련 메서드만)
package konkuk.thip.message.adapter.in.event;

import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
import konkuk.thip.message.application.port.in.FeedNotificationDispatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MessageFeedEventListener {

    private final FeedNotificationDispatchUseCase feedUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFollower(FeedEvents.FollowerEvent e) {
        feedUseCase.handleFollower(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedCommented(FeedEvents.FeedCommentedEvent e) {
        feedUseCase.handleFeedCommented(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedCommentReplied(FeedEvents.FeedCommentRepliedEvent e) {
        feedUseCase.handleFeedCommentReplied(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFolloweeNewPost(FeedEvents.FolloweeNewPostEvent e) {
        feedUseCase.handleFolloweeNewPost(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedLiked(FeedEvents.FeedLikedEvent e) {
        feedUseCase.handleFeedLiked(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedCommentLiked(FeedEvents.FeedCommentLikedEvent e) {
        feedUseCase.handleFeedCommentLiked(e);
    }
}