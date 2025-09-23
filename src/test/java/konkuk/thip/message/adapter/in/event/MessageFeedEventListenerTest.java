// src/test/java/konkuk/thip/message/adapter/in/event/MessageFeedEventListenerTest.java
package konkuk.thip.message.adapter.in.event;

import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
import konkuk.thip.message.application.port.in.FeedNotificationDispatchUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[단위] MessageRoomEventListener 단위 테스트")
class MessageFeedEventListenerTest {

    @Autowired
    private org.springframework.context.ApplicationEventPublisher publisher;

    @MockitoBean
    private FeedNotificationDispatchUseCase feedUseCase;

    @Test
    @Transactional
    @DisplayName("FollowerEvent 발행 → 커밋 시 이벤트 리스너가 useCase.handleFollower 호출")
    void follower_isHandled_afterCommit() {
        // given
        var e = FeedEvents.FollowerEvent.builder()
                .title("title")
                .content("content")
                .notificationId(1L)
                .targetUserId(1L)
                .build();

        // when: 트랜잭션 안에서 이벤트 발행
        publisher.publishEvent(e);

        // THEN: AFTER_COMMIT 시점 만들어주기
        TestTransaction.flagForCommit();
        TestTransaction.end();  // 여기서 @TransactionalEventListener(AFTER_COMMIT) 실행

        // then
        verify(feedUseCase, times(1)).handleFollower(Mockito.eq(e));
    }
}
