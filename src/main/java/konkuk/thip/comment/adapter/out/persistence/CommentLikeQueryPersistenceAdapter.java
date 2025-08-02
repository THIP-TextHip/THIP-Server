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
        Object result = commentLikeJpaRepository.existsByUserIdAndCommentId(userId, commentId);

        // 테스트 환경 H2에서의 반환값
        if (result instanceof Boolean) {
            return (Boolean) result;
        } // 로컬, 배포 환경 Mysql에서의 반환값
        else if (result instanceof Number) {
            return ((Number) result).longValue() > 0;
        }
        return false;
    }
}
