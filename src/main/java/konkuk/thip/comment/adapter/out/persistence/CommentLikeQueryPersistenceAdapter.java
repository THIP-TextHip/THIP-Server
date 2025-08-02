package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.mapper.CommentLikeMapper;
import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentLikeQueryPersistenceAdapter implements CommentLikeQueryPort {

    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public boolean isLikedCommentByUser(Long userId, Long commentId) {
        Long exists = commentLikeJpaRepository.existsByUserIdAndCommentId(userId, commentId);
        return exists != null && exists > 0;
    }
}
