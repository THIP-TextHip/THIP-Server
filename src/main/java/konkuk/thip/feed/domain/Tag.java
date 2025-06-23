package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Tag extends BaseDomainEntity {

    private Long id;

    private String value;

    private Long targetPostId;

    private Long categoryId;
}
