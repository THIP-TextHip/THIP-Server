package konkuk.thip.message.application.port.in;

import konkuk.thip.message.adapter.out.event.dto.RoomEvents;

public interface RoomNotificationDispatchUseCase {
    void handleRoomPostCommented(RoomEvents.RoomPostCommentedEvent e);

    void handleRoomVoteStarted(RoomEvents.RoomVoteStartedEvent e);

    void handleRoomRecordCreated(RoomEvents.RoomRecordCreatedEvent e);

    void handleRoomRecruitClosedEarly(RoomEvents.RoomRecruitClosedEarlyEvent e);

    void handleRoomActivityStarted(RoomEvents.RoomActivityStartedEvent e);

    void handleRoomJoinRequestedToOwner(RoomEvents.RoomJoinRequestedToOwnerEvent e);

    void handleRoomCommentLiked(RoomEvents.RoomCommentLikedEvent e);

    void handleRoomPostLiked(RoomEvents.RoomPostLikedEvent e);

    void handleRoomPostCommentReplied(RoomEvents.RoomPostCommentRepliedEvent e);
}