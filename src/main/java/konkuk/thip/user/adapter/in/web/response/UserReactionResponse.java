package konkuk.thip.user.adapter.in.web.response;

import java.util.List;

public record UserReactionResponse(
    List<ReactionDto> reactionList,
    String nextCursor,
    Boolean isLast
) {
    public record ReactionDto(
        String label,
        Long feedId,
        Long postId,
        String writer,
        Long writerId,
        String type,
        String content,
        String postDate
    ) {
    }

    public static UserReactionResponse of(
        List<ReactionDto> reactionList,
        String nextCursor,
        Boolean isLast
    ) {
        return new UserReactionResponse(reactionList, nextCursor, isLast);
    }
}
