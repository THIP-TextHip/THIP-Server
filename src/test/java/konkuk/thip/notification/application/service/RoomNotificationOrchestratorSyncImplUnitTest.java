package konkuk.thip.notification.application.service;

import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[단위] 모임방 알림 (동기화 방식) 헬퍼 서비스 단위 테스트")
class RoomNotificationOrchestratorSyncImplUnitTest {

    @Mock NotificationSyncExecutor notificationSyncExecutor;
    @Mock RoomEventCommandPort roomEventCommandPort;

    @InjectMocks RoomNotificationOrchestratorSyncImpl sut;

    @Test
    @DisplayName("모임방 게시글에 댓글: NotificationSyncExecutor 실행 (= DB notification 저장 + 이벤트 퍼블리시)")
    void notify_room_post_commented() {
        // given
        Long targetUserId = 10L;
        Long actorUserId = 20L;
        String actorUsername = "alice";
        Long roomId = 1L; int page = 2; Long postId = 3L; String postType = "RECORD";

        // when
        sut.notifyRoomPostCommented(targetUserId, actorUserId, actorUsername, roomId, page, postId, postType);

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
        verify(roomEventCommandPort).publishRoomPostCommentedEvent(
                "title", "content", 123L, targetUserId
        );
    }
}
