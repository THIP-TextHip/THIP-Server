package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
@SuperBuilder
public class Room extends BaseDomainEntity {

    private Long id;

    private String title;

    private String description;

    private boolean isPublic;

    private Integer password;

    private double roomPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private int recruitCount;

    private Long bookId;

    private Long categoryId;

    public boolean isExpired() {
        return this.getStatus() == StatusType.EXPIRED;
    }

    public void updateRoomPercentage(double roomPercentage) {
        this.roomPercentage = roomPercentage;
    }

    public void verifyPassword(String password) {

        // 모집기간 만료 체크
        LocalDate deadline = this.startDate.minusDays(1);
        if (isRecruitmentPeriodExpired()) {
            String message = String.format("모집기간(%s까지)이 만료된 방에는 참여할 수 없습니다.", deadline);
            throw new BusinessException(
                    ErrorCode.ROOM_RECRUITMENT_PERIOD_EXPIRED, new IllegalArgumentException(message)
            );
        }

        // 공개방일 경우 비밀번호 입력 요청 예외 처리
        if (this.isPublic()) {
            throw new BusinessException(ROOM_PASSWORD_NOT_REQUIRED);
        }

        //비밀번호 해싱 로직 추가되면 해싱 해제 하고 검증하는 로직추가
        if (this.password == null || !this.password.toString().equals(password)) {
            throw new BusinessException(ROOM_PASSWORD_MISMATCH);
        }
    }

    public boolean isRecruitmentPeriodExpired() {
        LocalDate today = LocalDate.now();
        // 모집 마감일: startDate.minusDays(1)
        return today.isAfter(this.startDate.minusDays(1));
    }

}
