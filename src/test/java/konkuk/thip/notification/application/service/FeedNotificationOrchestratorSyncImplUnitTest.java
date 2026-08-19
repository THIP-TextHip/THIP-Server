package konkuk.thip.notification.application.service;

import konkuk.thip.message.application.port.out.FeedEventCommandPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import konkuk.thip.user.application.port.out.UserBlockQueryPort;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위] 피드 알림 (동기화 방식) 헬퍼 서비스 단위 테스트")
class FeedNotificationOrchestratorSyncImplUnitTest {

    @Mock NotificationSyncExecutor notificationSyncExecutor;
    @Mock FeedEventCommandPort feedEventCommandPort;
    @Mock UserBlockQueryPort userBlockQueryPort;

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
                any(),                // template
                any(),                // args
                eq(targetUserId),     // targetUserId
                any(),                // redirectSpec
                invokerCaptor.capture() // invoker
        );

        // then: invoker 가 EventCommandPort 메서드를 올바르게 호출하는지 검증
        EventCommandInvoker invoker = invokerCaptor.getValue();
        invoker.publish("title", "content", 123L);
        verify(feedEventCommandPort).publishFeedCommentedEvent(
                "title", "content", 123L, targetUserId
        );
    }

    @Test
    @DisplayName("차단 관계면 알림을 만들지 않는다")
    void suppress_notification_when_blocked() {
        // given
        Long targetUserId = 10L;
        Long actorUserId = 20L;
        given(userBlockQueryPort.existsBlockBetween(targetUserId, actorUserId)).willReturn(true);

        // when
        sut.notifyFeedCommented(targetUserId, actorUserId, "alice", 99L);

        // then
        verify(notificationSyncExecutor, never()).execute(any(), any(), any(), any(), any());
    }
}
