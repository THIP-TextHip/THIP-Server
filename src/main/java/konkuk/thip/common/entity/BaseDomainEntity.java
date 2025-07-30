package konkuk.thip.common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class BaseDomainEntity {

    @Setter
    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private StatusType status;

    protected void changeStatus() {
        if (this.status == StatusType.ACTIVE) {
            this.status = StatusType.INACTIVE;
        } else {
            this.status = StatusType.ACTIVE;
        }
    }
}
