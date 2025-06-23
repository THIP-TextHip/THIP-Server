package konkuk.thip.domain.comment.adapter.out.persistence;

import konkuk.thip.domain.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.domain.comment.application.port.out.CommentQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentQueryPersistenceAdapter implements CommentQueryPort {

    private final CommentJpaRepository jpaRepository;
    private final CommentMapper userMapper;

}
