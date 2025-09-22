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
    public void publishRoomPostCommentedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long roomId, Integer page, Long postId, String postType) {
        publisher.publishEvent(RoomEvents.RoomPostCommentedEvent.builder()
                .title(title)
                .content(content)
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
    public void publishRoomVoteStartedEvent(
            String title, String content,
            Long targetUserId, Long roomId, String roomTitle,
            Integer page, Long postId) {
        publisher.publishEvent(RoomEvents.RoomVoteStartedEvent.builder()
                .title(title)
                .content(content)
                .targetUserId(targetUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .page(page)
                .postId(postId)
                .build());
    }

    @Override
    public void publishRoomRecordCreatedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long roomId, String roomTitle, Integer page, Long postId) {
        publisher.publishEvent(RoomEvents.RoomRecordCreatedEvent.builder()
                .title(title)
                .content(content)
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
    public void publishRoomRecruitClosedEarlyEvent(
            String title, String content,
            Long targetUserId, Long roomId, String roomTitle) {
        publisher.publishEvent(RoomEvents.RoomRecruitClosedEarlyEvent.builder()
                .title(title)
                .content(content)
                .targetUserId(targetUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .build());
    }

    @Override
    public void publishRoomActivityStartedEvent(
            String title, String content,
            Long targetUserId, Long roomId, String roomTitle) {
        publisher.publishEvent(RoomEvents.RoomActivityStartedEvent.builder()
                .title(title)
                .content(content)
                .targetUserId(targetUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .build());
    }

    @Override
    public void publishRoomJoinEventToHost(
            String title, String content,
            Long hostUserId, Long roomId, String roomTitle,
            Long actorUserId, String actorUsername) {
        publisher.publishEvent(RoomEvents.RoomJoinRequestedToOwnerEvent.builder()
                .title(title)
                .content(content)
                .ownerUserId(hostUserId)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .applicantUserId(actorUserId)
                .applicantUsername(actorUsername)
                .build());
    }

    @Override
    public void publishRoomCommentLikedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long roomId, Integer page, Long postId, String postType) {
        publisher.publishEvent(RoomEvents.RoomCommentLikedEvent.builder()
                .title(title)
                .content(content)
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
    public void publishRoomPostLikedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername,
            Long roomId, Integer page, Long postId, String postType) {
        publisher.publishEvent(RoomEvents.RoomPostLikedEvent.builder()
                .title(title)
                .content(content)
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
    public void publishRoomPostCommentRepliedEvent(
            String title, String content,
            Long targetUserId, Long actorUserId, String actorUsername, Long roomId, Integer page, Long postId, String postType) {
        publisher.publishEvent(RoomEvents.RoomPostCommentRepliedEvent.builder()
                .title(title)
                .content(content)
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