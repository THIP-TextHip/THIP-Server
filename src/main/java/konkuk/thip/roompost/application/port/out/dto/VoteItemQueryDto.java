package konkuk.thip.roompost.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.springframework.util.Assert;

public record VoteItemQueryDto(
        Long voteId,
        Long voteItemId,
        String itemName,
        Integer voteCount,
        Boolean isVoted
) {
    @QueryProjection
    public VoteItemQueryDto {
        Assert.notNull(voteId, "voteId must not be null");
        Assert.notNull(voteItemId, "voteItemId must not be null");
        Assert.notNull(itemName, "itemName must not be null");
        Assert.notNull(voteCount, "voteCount must not be null");
        Assert.notNull(isVoted, "isVoted must not be null");
    }
}
