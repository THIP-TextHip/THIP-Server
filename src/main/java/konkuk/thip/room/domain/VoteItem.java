package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class VoteItem extends BaseDomainEntity {

    private Long id;

    private String itemName;

    private int count;

    private Long voteId;
}
