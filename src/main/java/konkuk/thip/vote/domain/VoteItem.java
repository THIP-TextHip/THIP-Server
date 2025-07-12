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

    //todo 총 퍼센트가 100이 되는 알고리즘으로 수정!
    public int calculatePercentage(int totalCount) {
        return totalCount == 0 ? 0 : (int) Math.round((this.count * 100.0) / totalCount);
    }
}
