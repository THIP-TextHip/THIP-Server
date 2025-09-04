package konkuk.thip.message.adapter.out.event;

import konkuk.thip.message.adapter.out.event.dto.RoomEvents;
import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomEventPublisherAdapter implements RoomEventCommandPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishRoomRecordCommentedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                                Long roomId, Integer page, Long postId, String postType) {
        publisher.publishEvent(RoomEvents.RoomRecordCommentedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .roomId(roomId)
                .page(page)
                .postId(postId)
                .postType(postType)
                .build());
    }

    @Override
    public void publishRoomVoteStartedEvent(Long targetUserId, Long roomId, String roomTitle,
                                            Integer page, Long postId) {
        publisher.publishEvent(RoomEvents.RoomVoteStartedEvent.builder()
                .targetUserId(targetUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .page(page)
                .postId(postId)
                .build());
    }

    @Override
    public void publishRoomRecordCreatedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                              Long roomId, String roomTitle, Integer page, Long postId) {
        publisher.publishEvent(RoomEvents.RoomRecordCreatedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .page(page)
                .postId(postId)
                .build());
    }

    @Override
    public void publishRoomRecruitClosedEarlyEvent(Long targetUserId, Long roomId, String roomTitle) {
        publisher.publishEvent(RoomEvents.RoomRecruitClosedEarlyEvent.builder()
                .targetUserId(targetUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .build());
    }

    @Override
    public void publishRoomActivityStartedEvent(Long targetUserId, Long roomId, String roomTitle) {
        publisher.publishEvent(RoomEvents.RoomActivityStartedEvent.builder()
                .targetUserId(targetUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .build());
    }

    @Override
    public void publishRoomJoinEventToHost(Long ownerUserId, Long roomId, String roomTitle,
                                           Long applicantUserId, String applicantUsername) {
        publisher.publishEvent(RoomEvents.RoomJoinRequestedToOwnerEvent.builder()
                .ownerUserId(ownerUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .applicantUserId(applicantUserId)
                .applicantUsername(applicantUsername)
                .build());
    }

    @Override
    public void publishRoomCommentLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                             Long roomId, Integer page, Long postId) {
        publisher.publishEvent(RoomEvents.RoomCommentLikedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .roomId(roomId)
                .page(page)
                .postId(postId)
                .build());
    }

    @Override
    public void publishRoomRecordLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                            Long roomId, Integer page, Long postId, String postType) {
        publisher.publishEvent(RoomEvents.RoomRecordLikedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .roomId(roomId)
                .page(page)
                .postId(postId)
                .postType(postType)
                .build());
    }
}