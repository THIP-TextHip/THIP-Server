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
}
