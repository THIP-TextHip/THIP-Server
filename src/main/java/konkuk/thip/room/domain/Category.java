package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Category extends BaseDomainEntity {

    private Long id;

    private String value;

    private Long aliasId;
}
