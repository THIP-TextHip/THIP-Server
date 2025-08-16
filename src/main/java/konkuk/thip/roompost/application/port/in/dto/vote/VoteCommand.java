package konkuk.thip.roompost.application.port.in.dto.vote;

public record VoteCommand(
        Long userId,
        Long roomId,
        Long voteId,
        Long voteItemId,
        Boolean type
) {
}
