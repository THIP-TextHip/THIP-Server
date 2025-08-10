package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CommentQueryPersistenceAdapter implements CommentQueryPort {

    private final CommentJpaRepository commentJpaRepository;
    private final CommentMapper commentMapper;

    @Override
    public CursorBasedList<CommentQueryDto> findLatestRootCommentsWithDeleted(Long postId, String postTypeStr, Cursor cursor) {
        LocalDateTime lastCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<CommentQueryDto> commentQueryDtos = commentJpaRepository.findRootCommentsWithDeletedByCreatedAtDesc(postId, postTypeStr, lastCreatedAt, size);

        return CursorBasedList.of(commentQueryDtos, size, commentQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(commentQueryDto.createdAt().toString()));
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
}
