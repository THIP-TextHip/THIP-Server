package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.comment.adapter.out.mapper.CommentLikeMapper;
import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.comment.application.port.out.CommentLikeQueryPort;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.comment.domain.LikedComments;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class CommentLikeQueryPersistenceAdapter implements CommentLikeQueryPort {

    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public LikedComments findLikedCommentsByUserId(Long userId) {

        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        List<CommentLikeJpaEntity> likedCommentEntities = commentLikeJpaRepository.findAllByUserId((user.getUserId()));

        List<Comment> comments = likedCommentEntities.stream()
                .map(entity -> commentMapper.toDomainEntity(entity.getCommentJpaEntity()))
                .collect(Collectors.toList());

        return new LikedComments(comments);
    }
}
