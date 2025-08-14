package konkuk.thip.comment.adapter.in.web.response;

import java.util.List;

public record CommentForSinglePostResponse(
        List<RootCommentDto> commentList,
        String nextCursor,
        boolean isLast
) {
    public record RootCommentDto(
            Long commentId,
            Long creatorId,
            String creatorProfileImageUrl,
            String creatorNickname,
            String aliasName,
            String aliasColor,
            String postDate,        // 댓글 작성 시각 (~ 전 형식)
            String content,
            int likeCount,
            boolean isLike,
            boolean isDeleted,  // 삭제된 댓글인지 아닌지
            List<ReplyDto> replyList
    ) {
        public record ReplyDto(
                Long commentId,
                String parentCommentCreatorNickname,
                Long creatorId,
                String creatorProfileImageUrl,
                String creatorNickname,
                String aliasName,
                String aliasColor,
                String postDate,        // 댓글 작성 시각 (~ 전 형식)
                String content,
                int likeCount,
                boolean isLike
        ) {}

        /**
         * 삭제된 루트 댓글에 매핑되는 response dto
         * isDelete 제외 나머지 데이터는 모두 쓰레기 값으로
         */
        public static RootCommentDto createDeletedRootCommentDto(List<ReplyDto> replyList) {
            return new RootCommentDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    false,
                    true,       // true
                    replyList);
        }
    }
}
