package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class CommentLikeCommandPersistenceAdapter implements CommentLikeCommandPort {

    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final CommentJpaRepository commentJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(Long userId, Long commentId) {

        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        CommentJpaEntity comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND));

        CommentLikeJpaEntity commentLike = CommentLikeJpaEntity.builder()
                .userJpaEntity(user)
                .commentJpaEntity(comment)
                .build();

        commentLikeJpaRepository.save(commentLike);
    }

    @Override
    public void delete(Long userId, Long commentId) {
        commentLikeJpaRepository.deleteByUserIdAndCommentId(userId, commentId);
    }

    @Override
    public void deleteAllByCommentId(Long commentId) {
        commentLikeJpaRepository.deleteAllByCommentId(commentId);
    }

}
