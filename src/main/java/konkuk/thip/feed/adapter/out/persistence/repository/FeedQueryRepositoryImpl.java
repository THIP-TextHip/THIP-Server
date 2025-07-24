package konkuk.thip.feed.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.feed.adapter.out.jpa.ContentJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.QContentJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.QFeedJpaEntity;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.adapter.out.jpa.QPostLikeJpaEntity;
import konkuk.thip.saved.adapter.out.jpa.QSavedFeedJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final QFeedJpaEntity feed = QFeedJpaEntity.feedJpaEntity;
    private final QContentJpaEntity content = QContentJpaEntity.contentJpaEntity;
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
    private final QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;
    private final QBookJpaEntity book = QBookJpaEntity.bookJpaEntity;
    private final QSavedFeedJpaEntity savedFeed = QSavedFeedJpaEntity.savedFeedJpaEntity;
    private final QPostLikeJpaEntity postLike = QPostLikeJpaEntity.postLikeJpaEntity;
    private final QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        Set<Long> userIds = new HashSet<>(
                jpaQueryFactory
                        .select(feed.userJpaEntity.userId)
                        .distinct()
                        .from(feed)
                        .where(feed.bookJpaEntity.bookId.eq(bookId))
                        .fetch()
        );
        return userIds;
    }

    @Override
    public List<FeedQueryDto> findFeedsByFollowingPriority(Long userId, LocalDateTime cursorVal, int size) {
        // 1) 게시글 ID만 우선순위 + 페이징으로 조회
        List<Long> feedIds = fetchFeedIdsByFollowingPriority(userId, cursorVal, size);
        if (feedIds.isEmpty()) {
            return List.of();       // early return
        }

        // 2) 상세 엔티티를 ID 순으로 조회 후 정렬
        List<FeedJpaEntity> entities = fetchFeedEntitiesByIds(feedIds);
        Map<Long, FeedJpaEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(FeedJpaEntity::getPostId, e -> e));
        List<FeedJpaEntity> ordered = feedIds.stream()
                .map(entityMap::get)
                .toList();

        // 3) 플래그 조회
        Set<Long> savedSet = fetchSavedSet(userId, feedIds);
        Set<Long> likedSet = fetchLikedSet(userId, feedIds);

        // 4) DTO 변환
        return mapToDtoList(ordered, savedSet, likedSet);
    }

    @Override
    public List<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime cursorVal, int size) {
        // 1) 게시글 ID만 최신순 페이징으로 조회
        List<Long> feedIds = fetchFeedIdsLatest(userId, cursorVal, size);
        if (feedIds.isEmpty()) {
            return List.of();       // early return
        }

        // 2) 상세 엔티티 조회 및 정렬
        List<FeedJpaEntity> entities = fetchFeedEntitiesByIds(feedIds);
        Map<Long, FeedJpaEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(FeedJpaEntity::getPostId, e -> e));
        List<FeedJpaEntity> ordered = feedIds.stream()
                .map(entityMap::get)
                .toList();

        // 3) 플래그 조회
        Set<Long> savedSet = fetchSavedSet(userId, feedIds);
        Set<Long> likedSet = fetchLikedSet(userId, feedIds);

        // 4) DTO 변환
        return mapToDtoList(ordered, savedSet, likedSet);
    }

    /**
     * ID 목록만 우선순위 & 커서 페이징으로 조회
     */
    private List<Long> fetchFeedIdsByFollowingPriority(Long userId, LocalDateTime cursorVal, int size) {
        // 내가 작성한 모든 글 + 내가 팔로우하는 다른 유저가 작성한 공개글을 우선적으로 최신순 조회
        // 이후 내가 팔로우하지 않는 다른 유저가 작성한 공개글을 최신순 조회
        NumberExpression<Integer> priority = new CaseBuilder()
                .when(feed.userJpaEntity.userId.eq(userId)).then(1)
                .when(
                        following.userJpaEntity.userId.eq(userId)
                                .and(following.followingUserJpaEntity.userId.eq(feed.userJpaEntity.userId))
                                .and(feed.isPublic.eq(true))
                ).then(1)
                .otherwise(0);

        return jpaQueryFactory
                .select(feed.postId)
                .distinct()
                .from(feed)
                .leftJoin(following)
                .on(following.userJpaEntity.userId.eq(userId)
                        .and(following.followingUserJpaEntity.userId.eq(feed.userJpaEntity.userId)))
                .where(
                        // ACTIVE 인 feed & (내가 작성한 글 or 다른 유저가 작성한 공개글)
                        feed.status.eq(StatusType.ACTIVE),
                        feed.userJpaEntity.userId.eq(userId).or(feed.isPublic.eq(true)),
                        cursorVal != null ? feed.createdAt.lt(cursorVal) : Expressions.TRUE
                )
                .orderBy(priority.desc(), feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * ID 목록만 최신순 커서 페이징으로 조회
     */
    private List<Long> fetchFeedIdsLatest(Long userId, LocalDateTime cursorVal, int size) {
        return jpaQueryFactory
                .select(feed.postId)
                .distinct()
                .from(feed)
                .where(
                        // ACTIVE 인 feed & (내가 작성한 글 or 다른 유저가 작성한 공개글)
                        feed.status.eq(StatusType.ACTIVE),
                        feed.userJpaEntity.userId.eq(userId).or(feed.isPublic.eq(true)),
                        cursorVal != null ? feed.createdAt.lt(cursorVal) : Expressions.TRUE
                )
                .orderBy(feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * 주어진 ID 목록으로 엔티티를 페치조인 후 조회
     */
    private List<FeedJpaEntity> fetchFeedEntitiesByIds(List<Long> ids) {
        return jpaQueryFactory
                .select(feed).distinct()
                .from(feed)
                .leftJoin(feed.contentList, content).fetchJoin()
                .leftJoin(feed.userJpaEntity, user).fetchJoin()
                .leftJoin(user.aliasForUserJpaEntity, alias).fetchJoin()
                .leftJoin(feed.bookJpaEntity, book).fetchJoin()
                .where(feed.postId.in(ids))
                .fetch();
    }

    /**
     * 엔티티 목록 -> FeedQueryDto 목록 변환
     */
    private List<FeedQueryDto> mapToDtoList(List<FeedJpaEntity> entities, Set<Long> savedSet, Set<Long> likedSet) {
        return entities.stream()
                .map(e -> {
                    String[] urls = e.getContentList().stream()
                            .map(ContentJpaEntity::getContentUrl)
                            .toArray(String[]::new);
                    return new FeedQueryDto(
                            e.getPostId(),
                            e.getUserJpaEntity().getUserId(),
                            e.getUserJpaEntity().getNickname(),
                            e.getUserJpaEntity().getImageUrl(),
                            e.getUserJpaEntity().getAliasForUserJpaEntity().getValue(),
                            e.getCreatedAt(),
                            e.getBookJpaEntity().getIsbn(),
                            e.getBookJpaEntity().getTitle(),
                            e.getBookJpaEntity().getAuthorName(),
                            e.getContent(),
                            urls,
                            e.getLikeCount(),
                            e.getCommentCount(),
                            savedSet.contains(e.getPostId()),
                            likedSet.contains(e.getPostId())
                    );
                })
                .toList();
    }

    /** feedIds 중 해당 userId가 저장한(post) ID 조회 */
    private Set<Long> fetchSavedSet(Long userId, List<Long> feedIds) {
        return new HashSet<>(
                jpaQueryFactory
                        .select(savedFeed.feedJpaEntity.postId)
                        .from(savedFeed)
                        .where(
                                savedFeed.userJpaEntity.userId.eq(userId),
                                savedFeed.feedJpaEntity.postId.in(feedIds)
                        )
                        .fetch()
        );
    }

    /** feedIds 중 해당 userId가 좋아요 누른(post) ID 조회 */
    private Set<Long> fetchLikedSet(Long userId, List<Long> feedIds) {
        return new HashSet<>(
                jpaQueryFactory
                        .select(postLike.postJpaEntity.postId)
                        .from(postLike)
                        .where(
                                postLike.userJpaEntity.userId.eq(userId),
                                postLike.postJpaEntity.postId.in(feedIds)
                        )
                        .fetch()
        );
    }
}
