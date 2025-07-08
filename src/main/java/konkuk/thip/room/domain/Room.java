package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.entity.StatusType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

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
}
