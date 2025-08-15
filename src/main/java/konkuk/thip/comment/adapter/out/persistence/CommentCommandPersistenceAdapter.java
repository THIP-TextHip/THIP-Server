package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.entity.StatusType.ACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class CommentCommandPersistenceAdapter implements CommentCommandPort {

    private final CommentJpaRepository commentJpaRepository;
    private final FeedJpaRepository feedJpaRepository;
    private final RecordJpaRepository recordJpaRepository;
    private final VoteJpaRepository voteJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;

    private final CommentMapper commentMapper;

    @Override
    public Long save(Comment comment) {

        // 1. 작성자(User) 조회 및 존재 검증
        UserJpaEntity userJpaEntity = userJpaRepository.findById(comment.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        // 2. 게시물(Post) 조회 및 존재 검증
        PostJpaEntity postJpaEntity = findPostJpaEntity(comment.getPostType(), comment.getTargetPostId());

        // 3. 부모 댓글 조회 (있을 경우)
        CommentJpaEntity parentCommentJpaEntity = null;
        if (comment.getParentCommentId() != null) {
            parentCommentJpaEntity = commentJpaRepository.findById(comment.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND));
        }

        return commentJpaRepository.save(
                commentMapper.toJpaEntity(comment, postJpaEntity, userJpaEntity,parentCommentJpaEntity)
        ).getCommentId();
    }

    private PostJpaEntity findPostJpaEntity(PostType postType, Long postId) {
        return switch (postType) {
            case FEED -> feedJpaRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
            case RECORD -> recordJpaRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException(RECORD_NOT_FOUND));
            case VOTE -> voteJpaRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException(VOTE_NOT_FOUND));
        };
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentJpaRepository.findByCommentIdAndStatus(id, ACTIVE)
                .map(commentMapper::toDomainEntity);
    }

    @Override
    public void update(Comment comment) {
        CommentJpaEntity commentJpaEntity = commentJpaRepository.findById(comment.getId()).orElseThrow(
                () -> new EntityNotFoundException(COMMENT_NOT_FOUND)
        );

        commentJpaRepository.save(commentJpaEntity.updateFrom(comment));
    }

    @Override
    public void delete(Comment comment) {
        CommentJpaEntity commentJpaEntity = commentJpaRepository.findById(comment.getId()).orElseThrow(
                () -> new EntityNotFoundException(COMMENT_NOT_FOUND)
        );
        commentJpaRepository.delete(commentJpaEntity);
    }

    @Override
    public void softDeleteAllByPostId(Long postId) {
        commentLikeJpaRepository.deleteAllByPostId(postId);
        commentJpaRepository.softDeleteAllByPostId(postId);
    }

}
