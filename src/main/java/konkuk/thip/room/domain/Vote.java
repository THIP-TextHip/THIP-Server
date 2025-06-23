package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Vote extends BaseDomainEntity {

    private Long id;

    private String content;

    private Long creatorId;

    private Integer page;

    private boolean isOverview;

    private Long roomId;
}
