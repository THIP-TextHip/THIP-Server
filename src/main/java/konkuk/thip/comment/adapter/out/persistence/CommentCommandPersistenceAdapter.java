package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.mapper.CommentMapper;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
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
        // 2. 탈퇴한 유저의 모든 댓글, 댓글의 좋아요 삭제
        commentLikeJpaRepository.deleteAllByUserId(userId);
        commentJpaRepository.deleteAllByUserId(userId);
        // 3. 게시글 타입별로 댓글 수 감소가 필요한 게시글 Map 생성
        Map<PostType, List<PostJpaEntity>> postsByType = new HashMap<>();
        for (CommentJpaEntity comment : commentsWithPosts) {
            PostJpaEntity post = comment.getPostJpaEntity();
            post = (PostJpaEntity) Hibernate.unproxy(post); // 프록시 강제 초기화 및 타입 변경
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));

            // 4. 엔티티에서 직접 게시글 댓글 수 감소
            PostType postType = PostType.from(post.getDtype());
            postsByType.computeIfAbsent(postType, k -> new ArrayList<>()).add(post);
        }
        // 5. 게시글 타입별로 저장 처리
        postsByType.forEach(this::savePostsJpaEntities);
    }



    private void savePostsJpaEntities(PostType postType, List<PostJpaEntity> posts) {
        switch (postType) {
            case FEED:
                feedJpaRepository.saveAll(posts.stream()
                        .filter(p -> p instanceof FeedJpaEntity)
                        .map(p -> (FeedJpaEntity) p)
                        .collect(Collectors.toList()));
                break;
            case RECORD:
                recordJpaRepository.saveAll(posts.stream()
                        .filter(p -> p instanceof RecordJpaEntity)
                        .map(p -> (RecordJpaEntity) p)
                        .collect(Collectors.toList()));
                break;
            case VOTE:
                voteJpaRepository.saveAll(posts.stream()
                        .filter(p -> p instanceof VoteJpaEntity)
                        .map(p -> (VoteJpaEntity) p)
                        .collect(Collectors.toList()));
                voteJpaRepository.flush();
                break;
        }
    }
}
