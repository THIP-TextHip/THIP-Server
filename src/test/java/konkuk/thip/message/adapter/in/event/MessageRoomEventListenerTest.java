package konkuk.thip.message.adapter.in.event;

import konkuk.thip.message.adapter.out.event.dto.RoomEvents;
import konkuk.thip.message.application.port.in.RoomNotificationDispatchUseCase;
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
class MessageRoomEventListenerTest {

    @Autowired
    private org.springframework.context.ApplicationEventPublisher publisher;

    @MockitoBean
    private RoomNotificationDispatchUseCase roomUseCase;

    @Test
    @Transactional
    @DisplayName("RoomPostCommentedEvent 발행 → 커밋 시 이벤트 리스너가 useCase.handleRoomPostCommented 호출")
    void roomPostCommented_isHandled_afterCommit() {
        var e = RoomEvents.RoomPostCommentedEvent.builder()
                .targetUserId(10L).actorUserId(20L).actorUsername("alice")
                .roomId(100L).page(12).postId(999L).postType("RECORD")
                .build();

        publisher.publishEvent(e);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        verify(roomUseCase, times(1)).handleRoomPostCommented(Mockito.eq(e));
    }
}