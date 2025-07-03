package konkuk.thip.vote.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public record VoteCreateCommand(
        Long userId,

        Long roomId,

        int page,

        boolean isOverview,

        String content,

        List<VoteItem> voteItems
) {
    @Getter
    @Builder
    public record VoteItem(String itemName) {}
}
