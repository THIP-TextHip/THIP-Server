package konkuk.thip.comment.application.service;

import konkuk.thip.comment.adapter.in.web.response.RootCommentsResponse;
import konkuk.thip.comment.application.mapper.CommentQueryMapper;
import konkuk.thip.comment.application.port.in.RootCommentShowUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentShowAllQuery;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.annotation.persistence.Unfiltered;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RootCommentShowService implements RootCommentShowUseCase {

    private static final int PAGE_SIZE = 10;
    private final CommentQueryPort commentQueryPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentQueryMapper commentQueryMapper;

    @Override
//    @Transactional(readOnly = true)
//    @Unfiltered
    public RootCommentsResponse showRootCommentsOfPost(CommentShowAllQuery query) {
        Cursor cursor = Cursor.from(query.cursorStr(), PAGE_SIZE);

        // 1. 루트 댓글 조회 (Port에서 캐싱 여부 자동 판단)
        CursorBasedList<CommentQueryDto> commentQueryDtoCursorBasedList =
                commentQueryPort.findLatestRootCommentsWithDeleted(query.postId(), cursor);
        List<CommentQueryDto> rootsInOrder = commentQueryDtoCursorBasedList.contents();

        // 2. 반환할 루트 댓글 중 유저가 좋아한 댓글 조회
        Set<Long> rootCommentIds = rootsInOrder.stream()
                .map(CommentQueryDto::commentId)
                .collect(Collectors.toUnmodifiableSet());
        Set<Long> likedCommentIds = commentLikeQueryPort.findCommentIdsLikedByUser(rootCommentIds, query.userId());

        // 3. response 매핑
        List<RootCommentsResponse.RootCommentDto> rootCommentResponses =
                buildRootCommentResponses(rootsInOrder, likedCommentIds, query.userId());

        return new RootCommentsResponse(
                rootCommentResponses,
                commentQueryDtoCursorBasedList.nextCursor(),
                commentQueryDtoCursorBasedList.isLast()
        );
    }

    private List<RootCommentsResponse.RootCommentDto> buildRootCommentResponses(
            List<CommentQueryDto> roots,
            Set<Long> likedCommentIds,
            Long userId) {
        List<RootCommentsResponse.RootCommentDto> responses = new ArrayList<>();
        for (CommentQueryDto root : roots) {
            // 삭제된 루트 댓글 처리
            if (root.isDeleted()) {
                // 자식 댓글이 있는 경우: 쓰레기 값으로 반환 (descendantCount와 isDeleted만 실제 값)
                if (root.descendantCount() > 0) {
                    responses.add(RootCommentsResponse.RootCommentDto.createDeletedRootCommentDto(root.descendantCount()));
                }
                // 자식 댓글이 없는 경우: 응답에서 제외
                continue;
            }

            // 정상 루트 댓글: 매퍼로 변환
            responses.add(commentQueryMapper.toRootCommentResponse(root, likedCommentIds, userId));
        }
        return responses;
    }
}
