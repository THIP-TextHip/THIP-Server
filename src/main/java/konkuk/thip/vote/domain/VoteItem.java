package konkuk.thip.vote.domain;

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

    public static VoteItem withoutId(String itemName, int count, Long voteId) {
        return VoteItem.builder()
                .id(null)
                .itemName(itemName)
                .count(count)
                .voteId(voteId)
                .build();
    }
}
