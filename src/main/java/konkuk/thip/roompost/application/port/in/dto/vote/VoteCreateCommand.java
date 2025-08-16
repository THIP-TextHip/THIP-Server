package konkuk.thip.roompost.application.port.in.dto.vote;

import java.util.List;

public record VoteCreateCommand(
        Long userId,

        Long roomId,

        int page,

        boolean isOverview,

        String content,

        List<VoteItemCreateCommand> voteItemCreateCommands
) {
    public record VoteItemCreateCommand(String itemName) {}
}
