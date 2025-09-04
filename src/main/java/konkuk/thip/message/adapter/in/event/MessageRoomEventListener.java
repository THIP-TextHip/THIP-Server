package konkuk.thip.message.adapter.in.event;

import konkuk.thip.message.adapter.out.event.dto.RoomEvents;
import konkuk.thip.message.application.port.in.RoomNotificationDispatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MessageRoomEventListener {

    private final RoomNotificationDispatchUseCase roomUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomPostCommented(RoomEvents.RoomPostCommentedEvent e) {
        roomUseCase.handleRoomPostCommented(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomVoteStarted(RoomEvents.RoomVoteStartedEvent e) {
        roomUseCase.handleRoomVoteStarted(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomRecordCreated(RoomEvents.RoomRecordCreatedEvent e) {
        roomUseCase.handleRoomRecordCreated(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomRecruitClosedEarly(RoomEvents.RoomRecruitClosedEarlyEvent e) {
        roomUseCase.handleRoomRecruitClosedEarly(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomActivityStarted(RoomEvents.RoomActivityStartedEvent e) {
        roomUseCase.handleRoomActivityStarted(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomJoinRequestedToOwner(RoomEvents.RoomJoinRequestedToOwnerEvent e) {
        roomUseCase.handleRoomJoinRequestedToOwner(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomCommentLiked(RoomEvents.RoomCommentLikedEvent e) {
        roomUseCase.handleRoomCommentLiked(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomPostLiked(RoomEvents.RoomPostLikedEvent e) {
        roomUseCase.handleRoomPostLiked(e);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomPostCommentReplied(RoomEvents.RoomPostCommentRepliedEvent e) {
        roomUseCase.handleRoomPostCommentReplied(e);
    }
}
