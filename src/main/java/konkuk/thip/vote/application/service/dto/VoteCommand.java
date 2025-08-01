package konkuk.thip.vote.application.service.dto;

public record VoteCommand(
        Long userId,
        Long roomId,
        Long voteId,
        Long voteItemId,
        Boolean type
) {
}
