package konkuk.thip.comment.application.service;

import konkuk.thip.comment.adapter.in.web.response.CommentForSinglePostResponse;
import konkuk.thip.comment.application.mapper.CommentQueryMapper;
import konkuk.thip.comment.application.port.in.CommentShowAllUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentShowAllQuery;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentShowAllService implements CommentShowAllUseCase {

    private static final int PAGE_SIZE = 10;
    private final CommentQueryPort commentQueryPort;
    private final CommentLikeQueryPort commentLikeQueryPort;
    private final CommentQueryMapper commentQueryMapper;

    @Override
    public CommentForSinglePostResponse showAllCommentsOfPost(CommentShowAllQuery query) {
        Cursor cursor = Cursor.from(query.cursorStr(), PAGE_SIZE);

        // 1. size 크기만큼의 루트 댓글 최신순 조회 -> 삭제된 루트 댓글 포함해서 전부 조회
        CursorBasedList<CommentQueryDto> commentQueryDtoCursorBasedList = commentQueryPort.findLatestRootCommentsWithDeleted(query.postId(), query.postType().getType(), cursor);
        List<CommentQueryDto> rootsInOrder = commentQueryDtoCursorBasedList.contents();

        // 2. 조회한 루트 댓글들의 전체 자식 댓귿들을(깊이 무관) 작성 시간순으로 조회
        Set<Long> rootCommentIds = rootsInOrder.stream()
                .map(CommentQueryDto::commentId)
                .collect(Collectors.toUnmodifiableSet());

        Map<Long, List<CommentQueryDto>> childrenMap = commentQueryPort.findAllActiveChildCommentsOldestFirst(rootCommentIds);
        
        // 3. 반환할 모든 댓글(루트 + 자식 모두 포함) 중 유저가 좋아한 댓글 조회
        Set<Long> allCommentIds = parseAllCommentIds(childrenMap);
        Set<Long> likedCommentIds = commentLikeQueryPort.findCommentIdsLikedByUser(allCommentIds, query.userId());

        // 4. response 매핑
        List<CommentForSinglePostResponse.RootCommentDto> rootCommentResponses = buildRootCommentResponses(rootsInOrder, childrenMap, likedCommentIds, query.userId());

        return new CommentForSinglePostResponse(
                rootCommentResponses,
                commentQueryDtoCursorBasedList.nextCursor(),
                commentQueryDtoCursorBasedList.isLast()
        );
    }

    private Set<Long> parseAllCommentIds(Map<Long, List<CommentQueryDto>> childrenMap) {
        Set<Long> allCommentIds = new HashSet<>(childrenMap.keySet());  // 루트 댓글들
        for (Long rootCommentId : childrenMap.keySet()) {
            childrenMap.get(rootCommentId).stream()
                    .map(CommentQueryDto::commentId)
                    .forEach(allCommentIds::add);
        }
        return allCommentIds;
    }

    private List<CommentForSinglePostResponse.RootCommentDto> buildRootCommentResponses(
            List<CommentQueryDto> roots,
            Map<Long, List<CommentQueryDto>> childrenMap,
            Set<Long> likedCommentIds,
            Long userId) {
        List<CommentForSinglePostResponse.RootCommentDto> responses = new ArrayList<>();
        for (CommentQueryDto root : roots) {
            List<CommentQueryDto> children = childrenMap.getOrDefault(root.commentId(), Collections.emptyList());
            // 삭제된 루트 댓글이면서 자식이 없는 경우 건너뛰기
            if (root.isDeleted() && children.isEmpty()) {
                continue;
            }
            responses.add(commentQueryMapper.toRootCommentResponseWithChildren(root, children, likedCommentIds, userId));
        }
        return responses;
    }
}
