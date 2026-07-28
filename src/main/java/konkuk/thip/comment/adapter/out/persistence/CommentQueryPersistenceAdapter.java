package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentQueryPersistenceAdapter implements CommentQueryPort {

    private final CommentJpaRepository commentJpaRepository;
//    private final CommentCacheAdapter commentCacheAdapter;

    /**
     * 루트 댓글 조회 (페이징)
     * - 첫 페이지: CommentCacheAdapter를 통한 캐시 조회
     * - 2페이지 이후: DB 직접 조회 (캐싱하지 않음)
     */
    @Override
    public CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, Cursor cursor) {
        int size = cursor.getPageSize();
        Long lastRootCommentId = cursor.isFirstRequest() ? null : cursor.getLong(0);
        List<CommentQueryDto> commentQueryDtos = commentJpaRepository.findRootCommentsWithDeletedByCreatedAtDesc(postId, lastRootCommentId, size);

//        List<CommentQueryDto> commentQueryDtos;
//        if (cursor.isFirstRequest()) {
//            // 첫 페이지: 캐시 조회
//            commentQueryDtos = commentCacheAdapter.findFirstPageRootCommentsFromCache(postId, size);
//        } else {
//            // 2페이지 이후: DB 직접 조회
//            Long lastRootCommentId = cursor.getLong(0);
//            commentQueryDtos = commentJpaRepository.findRootCommentsWithDeletedByCreatedAtDesc(postId, lastRootCommentId, size);
//        }

        return CursorBasedList.of(commentQueryDtos, size, commentQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(commentQueryDto.commentId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<CommentQueryDto> findAllDescendantComments(Long rootCommentId, Cursor cursor) {
        Long lastCommentId = cursor.isFirstRequest() ? null : cursor.getLong(0);
        int size = cursor.getPageSize();

        List<CommentQueryDto> commentQueryDtos = commentJpaRepository.findAllDescendantCommentsByCreatedAtAsc(rootCommentId, lastCommentId, size);

        return CursorBasedList.of(commentQueryDtos, size, commentQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(commentQueryDto.commentId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CommentQueryDto findRootCommentById(Long rootCommentId) {
        return commentJpaRepository.findRootCommentId(rootCommentId);
    }

    @Override
    public CommentQueryDto findChildCommentById(Long rootCommentId, Long replyCommentId) {
        return commentJpaRepository.findChildCommentId(rootCommentId, replyCommentId);
    }
}
