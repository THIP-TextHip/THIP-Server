package konkuk.thip.roompost.application.port.in.dto.vote;

public record VoteUpdateCommand(
        Long roomId,
        Long postId,
        Long userId,
        String content
) {
}
