package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.mapper.PostLikeMapper;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.common.exception.code.ErrorCode.RECORD_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.VOTE_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class PostLikeCommandPersistenceAdapter implements PostLikeCommandPort {

    private final PostLikeJpaRepository postLikeJpaRepository;
    private final FeedJpaRepository feedJpaRepository;
    private final RecordJpaRepository recordJpaRepository;
    private final VoteJpaRepository voteJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PostLikeMapper postLikeMapper;

    @Override
    public void save(Long userId, Long postId, PostType postType) {

        UserJpaEntity userJpaEntity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        PostJpaEntity postJpaEntity = findPostJpaEntity(postType, postId);

        postLikeJpaRepository.save(postLikeMapper.toJpaEntity(postJpaEntity, userJpaEntity));
    }

    @Override
    public void delete(Long userId, Long postId) {
        postLikeJpaRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        postLikeJpaRepository.deleteAllByPostId(postId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        // 1. 탈퇴 유저가 좋아요한 게시글을 JOIN 조회
        List<PostJpaEntity> likedPosts = postLikeJpaRepository.findAllPostsWithTypeByUserId(userId);
        if (likedPosts == null || likedPosts.isEmpty()) {
            return; // early return
        }

        // 2. 게시글 좋아요 수 감소
        for (PostJpaEntity post : likedPosts) {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        }

        // 3. 탈퇴한 유저의 모든 게시글 좋아요 삭제
        postLikeJpaRepository.deleteAllByUserId(userId);
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
