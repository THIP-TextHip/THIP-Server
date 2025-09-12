package konkuk.thip.notification.application.service;

import konkuk.thip.message.application.port.out.FeedEventCommandPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위] 피드 알림 (동기화 방식) 헬퍼 서비스 단위 테스트")
class FeedNotificationOrchestratorSyncImplUnitTest {

    @Mock NotificationSyncExecutor notificationSyncExecutor;
    @Mock FeedEventCommandPort feedEventCommandPort;

    @InjectMocks FeedNotificationOrchestratorSyncImpl sut;

    @Test
    @DisplayName("피드 댓글 알림: NotificationSyncExecutor 실행 (= DB notification 저장 + 이벤트 퍼블리시)")
    void notify_feed_commented_test() {
        // given
        Long targetUserId = 10L;
        Long actorUserId = 20L;
        String actorUsername = "alice";
        Long feedId = 99L;

        // when
        sut.notifyFeedCommented(targetUserId, actorUserId, actorUsername, feedId);

        // then: NotificationSyncExecutor 가 올바르게 호출되었는지 검증
        ArgumentCaptor<EventCommandInvoker> invokerCaptor = ArgumentCaptor.forClass(EventCommandInvoker.class);
        verify(notificationSyncExecutor).execute(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(targetUserId),
                invokerCaptor.capture()
        );

        // then: invoker 가 EventCommandPort 메서드를 올바르게 호출하는지 검증
        EventCommandInvoker invoker = invokerCaptor.getValue();
        invoker.publish("title", "content");
        verify(feedEventCommandPort).publishFeedCommentedEvent(
                "title", "content", targetUserId, actorUserId, actorUsername, feedId
        );
    }
}
