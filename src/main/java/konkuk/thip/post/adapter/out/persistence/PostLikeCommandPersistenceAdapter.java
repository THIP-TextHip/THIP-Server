package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.mapper.PostLikeMapper;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
