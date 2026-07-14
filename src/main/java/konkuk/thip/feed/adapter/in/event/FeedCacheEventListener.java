package konkuk.thip.feed.adapter.in.event;

import konkuk.thip.feed.adapter.out.cache.FeedCacheHandler;
import konkuk.thip.feed.adapter.out.event.dto.FeedCreatedEvent;
import konkuk.thip.feed.adapter.out.event.dto.FeedDeletedEvent;
import konkuk.thip.feed.adapter.out.event.dto.FeedUpdatedEvent;
import konkuk.thip.user.adapter.out.event.dto.UserWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FeedCacheEventListener {

    private final FeedCacheHandler feedCacheHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedCreatedEvent(FeedCreatedEvent event) {
        feedCacheHandler.updateTopIdsWithNewId(event.feedId());
        feedCacheHandler.getFeedDetail(event.feedId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedDeletedEvent(FeedDeletedEvent event) {
        feedCacheHandler.markAsDeleted(event.feedId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedUpdatedEvent(FeedUpdatedEvent event) {
        if (feedCacheHandler.evictFeedDetailIfPresent(event.feedId())) {
            feedCacheHandler.getFeedDetail(event.feedId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserWithdrawn(UserWithdrawnEvent event) {
        feedCacheHandler.refreshCacheAfterBulkDelete(event.deletedFeedIds());
    }
}