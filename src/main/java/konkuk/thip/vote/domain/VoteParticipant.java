package konkuk.thip.vote.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class VoteParticipant extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long voteItemId;
}
