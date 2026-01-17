package konkuk.thip.comment.application.service;

import konkuk.thip.comment.adapter.in.web.response.ChildCommentsResponse;
import konkuk.thip.comment.application.mapper.CommentQueryMapper;
import konkuk.thip.comment.application.port.in.ChildCommentsShowUseCase;
import konkuk.thip.comment.application.port.in.dto.ChildCommentsShowQuery;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildCommentsShowService implements ChildCommentsShowUseCase {

    private static final int PAGE_SIZE = 10;
    private final CommentQueryPort commentQueryPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentQueryMapper commentQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public ChildCommentsResponse showChildComments(ChildCommentsShowQuery query) {
        Cursor cursor = Cursor.from(query.cursorStr(), PAGE_SIZE);

        // 1. 특정 루트 댓글의 자식 댓글을 최신순으로 페이징 조회
        CursorBasedList<CommentQueryDto> childCommentsCursorBasedList = commentQueryPort.findChildComments(query.rootCommentId(), cursor);
        List<CommentQueryDto> childComments = childCommentsCursorBasedList.contents();

        // 2. 유저가 좋아한 댓글 조회
        Set<Long> childCommentIds = childComments.stream()
                .map(CommentQueryDto::commentId)
                .collect(Collectors.toUnmodifiableSet());
        Set<Long> likedCommentIds = commentLikeQueryPort.findCommentIdsLikedByUser(childCommentIds, query.userId());

        // 3. response 매핑
        List<ChildCommentsResponse.ChildCommentDto> childCommentResponses = buildChildCommentResponses(
                childComments, likedCommentIds, query.userId()
        );

        return new ChildCommentsResponse(
                childCommentResponses,
                childCommentsCursorBasedList.nextCursor(),
                childCommentsCursorBasedList.isLast()
        );
    }

    private List<ChildCommentsResponse.ChildCommentDto> buildChildCommentResponses(
            List<CommentQueryDto> childComments,
            Set<Long> likedCommentIds,
            Long userId) {
        return childComments.stream()
                .map(child -> commentQueryMapper.toChildComment(child, likedCommentIds, userId))
                .toList();
    }
}
