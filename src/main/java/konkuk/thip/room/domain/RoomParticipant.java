package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@SuperBuilder
public class RoomParticipant extends BaseDomainEntity {

    private Long id;

    private int currentPage;

    private double userPercentage;

    private String roomParticipantRole;

    private Long userId;

    private Long roomId;

    public static RoomParticipant withoutId(Long userId, Long roomId, String roomParticipantRole) {
        return RoomParticipant.builder()
                .currentPage(0)
                .userPercentage(0.0)
                .userId(userId)
                .roomId(roomId)
                .roomParticipantRole(roomParticipantRole)
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

    // 방장이 참여 취소를 요청한 경우
    public void cancelParticipation() {
        if (checkRole(RoomParticipantRole.HOST)) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_CANCEL);
        }
    }

    public void closeRoomJoin() {
        if (checkRole(RoomParticipantRole.MEMBER)) {
            throw new BusinessException(ErrorCode.MEMBER_CANNOT_CLOSE);
        }
    }

    private boolean checkRole(RoomParticipantRole host) {
        return Objects.equals(this.roomParticipantRole, host.getType());
    }


}
