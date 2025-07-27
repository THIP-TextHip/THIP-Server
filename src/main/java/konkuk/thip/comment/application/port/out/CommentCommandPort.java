package konkuk.thip.comment.application.port.out;


import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.exception.EntityNotFoundException;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.COMMENT_NOT_FOUND;

public interface CommentCommandPort {

    Long save(Comment comment);

    Optional<Comment> findById(Long id);

    default Comment getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND));
    }

    void update(Comment comment);

}
