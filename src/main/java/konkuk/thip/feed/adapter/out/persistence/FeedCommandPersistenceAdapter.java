package konkuk.thip.feed.adapter.out.persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.adapter.out.persistence.repository.FeedJpaRepository;
import konkuk.thip.feed.adapter.out.persistence.repository.SavedFeedJpaRepository;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.post.adapter.out.persistence.repository.PostLikeJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class FeedCommandPersistenceAdapter implements FeedCommandPort {

    private final JdbcTemplate jdbcTemplate;

    private final FeedJpaRepository feedJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final SavedFeedJpaRepository savedFeedJpaRepository;

    private final CommentJpaRepository commentJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final PostLikeJpaRepository postLikeJpaRepository;

    private final FeedMapper feedMapper;

    @Override
    public Optional<Feed> findById(Long id) {
        return feedJpaRepository.findByPostId(id)
                .map(feedMapper::toDomainEntity);
    }

    @Override
    public List<Long> findByIds(List<Long> ids) {
        return feedJpaRepository.findByPostIds(ids);
    }


    @Override
    public Long save(Feed feed) {

        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(feed.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(feed.getTargetBookId()).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );
        FeedJpaEntity feedJpaEntity = feedMapper.toJpaEntity(feed,userJpaEntity,bookJpaEntity);

        // Feed 먼저 영속화 → ID 생성
        FeedJpaEntity savedFeed = feedJpaRepository.save(feedJpaEntity);

        return savedFeed.getPostId();
    }

    @Override
    public Long update(Feed feed) {
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findByPostId(feed.getId())
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
        feedJpaEntity.updateFrom(feed);

        return feedJpaEntity.getPostId();
    }

    @Override
    public void saveSavedFeed(Long userId, Long feedId) {
        UserJpaEntity user = userJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        FeedJpaEntity feed = feedJpaRepository.findByPostId(feedId)
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
        SavedFeedJpaEntity entity = SavedFeedJpaEntity.builder()
                .userJpaEntity(user)
                .feedJpaEntity(feed)
                .build();
        savedFeedJpaRepository.save(entity);
    }

    @Override
    public void deleteSavedFeed(Long userId, Long feedId) {
        savedFeedJpaRepository.deleteByUserIdAndFeedId(userId, feedId);
    }

    @Override
    public void deleteAllSavedFeedByUserId(Long userId) {
        savedFeedJpaRepository.deleteAllByUserId(userId);
    }

    @Override
    public void deleteAllFeedByUserId(Long userId) {
        // 1. 유저가 작성한 피드 게시글 ID 리스트 조회
        List<Long> feedIds = feedJpaRepository.findFeedIdsByUserId(userId);
        // 1. 유저가 작성한 피드 게시글 ID 리스트 조회
        if (feedIds == null || feedIds.isEmpty()) {
            return; // early return
        }
        // 2-1. 댓글 좋아요 일괄 삭제
        commentLikeJpaRepository.deleteAllByPostIds(feedIds);
        // 2-2. 댓글 soft delete 일괄 처리
        commentJpaRepository.softDeleteAllByPostIds(feedIds);
        // 3. 게시글 좋아요 일괄 삭제
        postLikeJpaRepository.deleteAllByPostIds(feedIds);
        // 4. 피드 저장 일괄 삭제
        savedFeedJpaRepository.deleteAllByFeedIds(feedIds);
        // 5. 탈퇴한 유저가 작성한 피드 게시글 soft delete 일괄 처리
        feedJpaRepository.softDeleteAllByUserId(userId);
    }

    @Override
    public void batchUpdateLikeCounts(Map<Long, Integer> idToLikeCount) {
        String sql = "UPDATE posts SET like_count = like_count + ? WHERE post_id = ?";
        List<Map.Entry<Long, Integer>> entries = new ArrayList<>(idToLikeCount.entrySet());

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map.Entry<Long, Integer> entry = entries.get(i);
                ps.setInt(1, entry.getValue());  // Redis에서 가져온 좋아요 수 (증분 값)
                ps.setLong(2, entry.getKey());   //게시글 ID
            }

            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }

    @Override
    public void delete(Feed feed) {
        FeedJpaEntity feedJpaEntity = feedJpaRepository.findByPostId(feed.getId())
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));

        savedFeedJpaRepository.deleteAllByFeedId(feedJpaEntity.getPostId());

        feedJpaEntity.softDelete();
        feedJpaRepository.save(feedJpaEntity);
    }
}
