package konkuk.thip.vote.application.port.in.dto;

public record VoteCommand(
        Long userId,
        Long roomId,
        Long voteId,
        Long voteItemId,
        Boolean type
) {
}
