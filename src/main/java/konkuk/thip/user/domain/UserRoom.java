package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserRoom extends BaseDomainEntity {

    private Long id;

    private int currentPage;

    private double userPercentage;

    private String userRoomRole;

    private Long userId;

    private Long roomId;

    public boolean canWriteOverview() {
        return userPercentage >= 80;
    }

    // 기록(투표) 요청 페이지와 책 전체 페이지
    public boolean updateUserProgress(int requestPage, int totalPageCount) {
        if (currentPage < requestPage) {
            currentPage = requestPage;
            userPercentage = ((double) currentPage / totalPageCount) * 100;
            return true;
        }

        return false;
    }
}
