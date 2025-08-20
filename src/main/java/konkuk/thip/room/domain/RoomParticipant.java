package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_HOST_CANNOT_LEAVE;

@Getter
@SuperBuilder
public class RoomParticipant extends BaseDomainEntity {

    private Long id;

    private int currentPage;

    private double userPercentage;

    private String roomParticipantRole;

    private Long userId;

    private Long roomId;

    public static RoomParticipant memberWithoutId(Long userId, Long roomId) {
        return RoomParticipant.builder()
                .currentPage(0)
                .userPercentage(0.0)
                .userId(userId)
                .roomId(roomId)
                .roomParticipantRole(RoomParticipantRole.MEMBER.getType())
                .build();
    }

    public static RoomParticipant hostWithoutId(Long userId, Long roomId) {
        return RoomParticipant.builder()
                .currentPage(0)
                .userPercentage(0.0)
                .userId(userId)
                .roomId(roomId)
                .roomParticipantRole(RoomParticipantRole.HOST.getType())
                .build();
    }

    public boolean canWriteOverview() {
        return userPercentage >= 80;
    }

    // 기록(투표) 요청 페이지와 책 전체 페이지
    public boolean updateUserProgress(int requestPage, int totalPageCount) {
        if (currentPage < requestPage) {
            currentPage = requestPage;
            userPercentage = Math.min(((double) currentPage / totalPageCount) * 100, 100.0);
            return true;
        }

        return false;
    }

    public boolean isHost() {
        return checkRole(RoomParticipantRole.HOST);
    }

    public boolean isMember() {
        return checkRole(RoomParticipantRole.MEMBER);
    }

    private boolean checkRole(RoomParticipantRole roomParticipantRole) {
        return this.roomParticipantRole.equals(roomParticipantRole.getType());
    }

    public void validateRoomLeavable() {
        if (isHost()) {
            throw new InvalidStateException(ROOM_HOST_CANNOT_LEAVE);
        }
    }

}
