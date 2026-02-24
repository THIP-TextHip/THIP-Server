package konkuk.thip.feed.adapter.in.event;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import konkuk.thip.feed.adapter.out.cache.FeedCacheHandler;
import konkuk.thip.feed.adapter.out.event.dto.FeedCreatedEvent;
import konkuk.thip.feed.adapter.out.event.dto.FeedDeletedEvent;
import konkuk.thip.feed.adapter.out.event.dto.FeedUpdatedEvent;
import konkuk.thip.user.adapter.out.event.dto.UserWithdrawnEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[단위] FeedCacheEventListener 단위 테스트")
class FeedCacheEventListenerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockitoBean
    private FeedCacheHandler feedCacheHandler;

    @Test
    @Transactional
    @DisplayName("FeedCreatedEvent 발행 → 커밋 후 handleFeedCreatedEvent가 호출된다")
    void handleFeedCreatedEvent_Success() {
        // given
        Long feedId = 1L;
        FeedCreatedEvent event = FeedCreatedEvent.from(feedId);

        // when
        publisher.publishEvent(event);

        // 커밋 시점 강제 시뮬레이션
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(feedCacheHandler, times(1)).updateTopIdsWithNewId(feedId);
        verify(feedCacheHandler, times(1)).getFeedDetail(feedId);
    }

    @Test
    @Transactional
    @DisplayName("FeedDeletedEvent 이벤트 발행 → 커밋 후 handleFeedDeletedEvent가 호출된다")
    void handleFeedDeletedEvent_Success() {
        // given
        Long feedId = 1L;
        FeedDeletedEvent event = FeedDeletedEvent.from(feedId);

        // when
        publisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(feedCacheHandler, times(1)).markAsDeleted(feedId);
    }

    @Test
    @Transactional
    @DisplayName("FeedUpdatedEvent 이벤트 발행 → 커밋 후 handleFeedUpdatedEvent가 호출된다")
    void handleFeedUpdatedEvent_Success() {
        // given
        Long feedId = 1L;
        FeedUpdatedEvent event = FeedUpdatedEvent.from(feedId);

        // evictFeedDetailIfPresent가 true를 반환한다고 가정
        given(feedCacheHandler.evictFeedDetailIfPresent(feedId)).willReturn(true);

        // when
        publisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(feedCacheHandler, times(1)).evictFeedDetailIfPresent(feedId);
        verify(feedCacheHandler, times(1)).getFeedDetail(feedId);
    }

    @Test
    @Transactional
    @DisplayName("UserWithdrawnEvent 이벤트 발행 → 커밋 후 handleUserWithdrawn가 호출된다")
    void handleUserWithdrawn_Success() {
        // given
        Long userId = 1L;
        List<Long> deletedFeedIds = List.of(1L, 2L, 3L);
        UserWithdrawnEvent event = UserWithdrawnEvent.of(userId, deletedFeedIds);

        // when
        publisher.publishEvent(event);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(feedCacheHandler, times(1)).refreshCacheAfterBulkDelete(deletedFeedIds);
    }

}