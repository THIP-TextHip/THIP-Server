package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CommentQueryPersistenceAdapter implements CommentQueryPort {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, Cursor cursor) {
        Long lastRootCommentId = cursor.isFirstRequest() ? null : cursor.getLong(0);
        int size = cursor.getPageSize();

        List<CommentQueryDto> commentQueryDtos = commentJpaRepository.findRootCommentsWithDeletedByCreatedAtDesc(postId, lastRootCommentId, size);

        return CursorBasedList.of(commentQueryDtos, size, commentQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(commentQueryDto.commentId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public List<CommentQueryDto> findAllActiveChildCommentsOldestFirst(Long rootCommentId) {
        return commentJpaRepository.findAllActiveChildCommentsByCreatedAtAsc(rootCommentId);
    }

    @Override
    public Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsOldestFirst(Set<Long> rootCommentIds) {
        return commentJpaRepository.findAllActiveChildCommentsByCreatedAtAsc(rootCommentIds);
    }

    @Override
    public CursorBasedList<CommentQueryDto> findChildComments(Long rootCommentId, Cursor cursor) {
        Long lastChildCommentId = cursor.isFirstRequest() ? null : cursor.getLong(0);
        int size = cursor.getPageSize();

        List<CommentQueryDto> commentQueryDtos = commentJpaRepository.findChildCommentsByCreatedAtAsc(rootCommentId, lastChildCommentId, size);

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
