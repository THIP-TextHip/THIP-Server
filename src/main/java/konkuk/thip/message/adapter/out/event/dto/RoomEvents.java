package konkuk.thip.message.adapter.out.event.dto;

import lombok.Builder;

public class RoomEvents {

    // 댓글 대상이 "기록/투표" 모두 가능하므로 통합 스키마 사용
    // 내 모임방 기록/투표에 댓글이 달린 경우
    @Builder
    public record RoomPostCommentedEvent(
        String title, String content, Long notificationId,
        Long targetUserId) {}

    // 내가 참여한 모임방에 새로운 투표가 시작된 경우
    @Builder
    public record RoomVoteStartedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 참여한 모임방에 새로운 기록이 작성된 경우
    @Builder
    public record RoomRecordCreatedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 참여한 모임방이 조기 종료된 경우 (호스트가 모집 마감 버튼 누른 경우)
    @Builder
    public record RoomRecruitClosedEarlyEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 참여한 모임방 활동이 시작된 경우 (방이 시작 기간이 되어 자동으로 시작된 경우)
    @Builder
    public record RoomActivityStartedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 방장일 때, 새로운 사용자가 모임방 참여를 한 경우
    @Builder
    public record RoomJoinRequestedToOwnerEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 참여한 모임방의 나의 댓글이 좋아요를 받는 경우
    @Builder
    public record RoomCommentLikedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 참여한 모임방의 나의 기록이 좋아요를 받는 경우
    @Builder
    public record RoomPostLikedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}

    // 내가 참여한 모임방의 나의 댓글에 대댓글이 달린 경우
    @Builder
    public record RoomPostCommentRepliedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {}
}
