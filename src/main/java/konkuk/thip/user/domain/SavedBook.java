package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SavedBook extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long bookId;
}
