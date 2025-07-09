package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.application.port.out.CommentQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentQueryPersistenceAdapter implements CommentQueryPort {

    private final CommentJpaRepository jpaRepository;
    private final CommentMapper userMapper;

    @Override
    public int countByPostIdAndUserId(Long postId, Long userId) {
        return jpaRepository.countByPostJpaEntity_PostIdAndUserJpaEntity_UserId(postId, userId);
    }
}
