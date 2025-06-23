package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Feed extends BaseDomainEntity {

    private Long id;

    private String content;

    private Long creatorId;

    private Boolean isPublic;

    private int reportCount;

    private Long targetBookId;

}
