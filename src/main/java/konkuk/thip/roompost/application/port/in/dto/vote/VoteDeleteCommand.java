package konkuk.thip.roompost.application.port.in.dto.vote;

public record VoteDeleteCommand(
        Long roomId,

        Long voteId,

        Long userId
) {
}
