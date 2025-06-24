package konkuk.thip.common.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class BaseDomainEntity {

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private StatusType status;
}
