package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Notification extends BaseDomainEntity {

    private Long id;

    private String title;

    private String content;

    private boolean isChecked;

    private Long targetUserId;
}
