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

import java.util.*;
import java.util.stream.Collectors;

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
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(comment.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        // 2. 게시물(Post) 조회 및 존재 검증
        PostJpaEntity postJpaEntity = findPostJpaEntity(comment.getPostType(), comment.getTargetPostId());

        // 3. 부모 댓글 조회 (있을 경우)
        CommentJpaEntity parentCommentJpaEntity = null;
        if (comment.getParentCommentId() != null) {
            parentCommentJpaEntity = commentJpaRepository.findByCommentId(comment.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException(COMMENT_NOT_FOUND));
        }

        return commentJpaRepository.save(
                commentMapper.toJpaEntity(comment, postJpaEntity, userJpaEntity,parentCommentJpaEntity)
        ).getCommentId();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentJpaRepository.findByCommentId(id)
                .map(commentMapper::toDomainEntity);
    }

    @Override
    public void update(Comment comment) {
        CommentJpaEntity commentJpaEntity = commentJpaRepository.findByCommentId(comment.getId()).orElseThrow(
                () -> new EntityNotFoundException(COMMENT_NOT_FOUND)
        );

        commentJpaRepository.save(commentJpaEntity.updateFrom(comment));
    }

    @Override
    public void delete(Comment comment) {
        CommentJpaEntity commentJpaEntity = commentJpaRepository.findByCommentId(comment.getId()).orElseThrow(
                () -> new EntityNotFoundException(COMMENT_NOT_FOUND)
        );
        commentJpaRepository.delete(commentJpaEntity);
    }

    @Override
    public void softDeleteAllByPostId(Long postId) {
        commentLikeJpaRepository.deleteAllByPostId(postId);
        commentJpaRepository.softDeleteAllByPostId(postId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        // 1. 탈퇴 유저가 작성한 댓글과 연관된 게시글을 JOIN FETCH로 함께 조회
        List<CommentJpaEntity> commentsWithPosts = commentJpaRepository.findAllCommentsWithPostsByUserId(userId);
        if (commentsWithPosts == null || commentsWithPosts.isEmpty()) {
            return; //early return
        }

        // 2. 삭제될 댓글이 어느 Post에 몇 개씩 붙어있는지 집계 (postId 기준 추천)
        Map<PostJpaEntity, Long> decMap = commentsWithPosts.stream()
                .collect(Collectors.groupingBy(CommentJpaEntity::getPostJpaEntity, Collectors.counting()));

        // 3. 댓글 수를 집계만큼 한 번에 감소
        for (PostJpaEntity p : decMap.keySet()) {
            long dec = decMap.getOrDefault(p, 0L);
            p.setCommentCount(Math.max(0, p.getCommentCount() - (int) dec));
        }

        // 4. 탈퇴한 유저의 모든 댓글, 댓글의 좋아요 삭제
        commentLikeJpaRepository.deleteAllByCommentAuthorUserId(userId);
        commentJpaRepository.softDeleteAllByUserId(userId);
    }

    private PostJpaEntity findPostJpaEntity(PostType postType, Long postId) {
        return switch (postType) {
            case FEED -> feedJpaRepository.findByPostId(postId)
                    .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
            case RECORD -> recordJpaRepository.findByPostId(postId)
                    .orElseThrow(() -> new EntityNotFoundException(RECORD_NOT_FOUND));
            case VOTE -> voteJpaRepository.findByPostId(postId)
                    .orElseThrow(() -> new EntityNotFoundException(VOTE_NOT_FOUND));
        };
    }
}
