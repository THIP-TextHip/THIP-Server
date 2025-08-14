package konkuk.thip.vote.application.port.in.dto;

public record VoteDeleteCommand(
        Long roomId,

        Long voteId,

        Long userId
) {
}
