package konkuk.thip.comment.adapter.in.web.response;

import java.util.List;

public record RootCommentsResponse(
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
            boolean isWriter,
            int descendantCount  // 자식 댓글 수
    ) {
        /**
         * 삭제된 루트 댓글 생성용 정적 팩토리 메서드
         * descendantCount와 isDeleted만 실제 값이고, 나머지는 모두 쓰레기 값
         */
        public static RootCommentDto createDeletedRootCommentDto(int descendantCount) {
            return new RootCommentDto(
                    null,           // commentId
                    null,           // creatorId
                    null,           // creatorProfileImageUrl
                    null,           // creatorNickname
                    null,           // aliasName
                    null,           // aliasColor
                    null,           // postDate
                    null,           // content
                    0,              // likeCount
                    false,          // isLike
                    true,           // isDeleted (삭제됨)
                    false,          // isWriter
                    descendantCount // descendantCount (실제 자식 댓글 수)
            );
        }
    }
}
