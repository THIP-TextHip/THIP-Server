package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class RoomParticipant extends BaseDomainEntity {

    private Long id;

    private int currentPage;

    private double userPercentage;

    private String roomParticipantRole;

    private Long userId;

    private Long roomId;

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
}
