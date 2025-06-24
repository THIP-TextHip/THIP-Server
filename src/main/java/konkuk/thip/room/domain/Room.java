package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
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
}
