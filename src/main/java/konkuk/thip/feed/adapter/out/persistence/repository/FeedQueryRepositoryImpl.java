package konkuk.thip.feed.adapter.out.persistence.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.QFeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.QSavedFeedJpaEntity;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.application.port.out.dto.QFeedQueryDto;
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
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
    private final QBookJpaEntity book = QBookJpaEntity.bookJpaEntity;
    private final QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;
    private final QSavedFeedJpaEntity savedFeed = QSavedFeedJpaEntity.savedFeedJpaEntity;

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
    public List<FeedQueryDto> findFeedsByFollowingPriority(Long userId, Integer lastPriority, LocalDateTime lastCreatedAt, int size) {
        // 1) 게시글 ID만 우선순위 + 페이징으로 조회
        List<Tuple> tuples = fetchFeedIdsAndPriorityByFollowingPriority(userId, lastPriority, lastCreatedAt, size);
        if (tuples.isEmpty()) {
            return List.of();       // early return
        }

        // 2) 상세 엔티티를 ID 순으로 조회 후 정렬
        List<Long> feedIds = tuples.stream()
                .map(tuple -> tuple.get(0, Long.class))
                .toList();
        Map<Long, Integer> priorityMap = tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Long.class),
                        tuple -> tuple.get(1, Integer.class)
                ));

        List<FeedJpaEntity> entities = fetchFeedEntitiesByIds(feedIds);
        Map<Long, FeedJpaEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(FeedJpaEntity::getPostId, e -> e));
        List<FeedJpaEntity> ordered = feedIds.stream()
                .map(entityMap::get)
                .toList();

        // 3) DTO 변환
        return ordered.stream()
                .map(e -> toDto(e, priorityMap.get(e.getPostId())))
                .toList();
    }

    @Override
    public List<FeedQueryDto> findLatestFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size) {
        // 1) 게시글 ID만 최신순 페이징으로 조회
        List<Long> feedIds = fetchFeedIdsLatest(userId, lastCreatedAt, size);
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

        // 3) DTO 변환 (priority 없음)
        return ordered.stream()
                .map(e -> toDto(e, null))
                .toList();
    }

    /**
     * ID 목록만 우선순위 & 커서 페이징으로 조회
     */
    private List<Tuple> fetchFeedIdsAndPriorityByFollowingPriority(Long userId, Integer lastPriority, LocalDateTime lastCreatedAt, int size) {
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

        // 복합 커서 조건: 우선순위 및 생성일시 기준
        BooleanExpression cursorCondition = (lastPriority != null && lastCreatedAt != null)
                ? priority.lt(lastPriority)
                .or(priority.eq(lastPriority)
                        .and(feed.createdAt.lt(lastCreatedAt)))
                : Expressions.TRUE;

        return jpaQueryFactory
                .select(feed.postId, priority)
                .from(feed)
                .leftJoin(following)
                .on(following.userJpaEntity.userId.eq(userId)
                        .and(following.followingUserJpaEntity.userId.eq(feed.userJpaEntity.userId)))
                .where(
                        // ACTIVE 인 feed & (내가 작성한 글 or 다른 유저가 작성한 공개글) & cursorCondition
                        feed.status.eq(StatusType.ACTIVE),
                        feed.userJpaEntity.userId.eq(userId).or(feed.isPublic.eq(true)),
                        cursorCondition
                )
                .orderBy(priority.desc(), feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * ID 목록만 최신순 커서 페이징으로 조회
     */
    private List<Long> fetchFeedIdsLatest(Long userId, LocalDateTime lastCreatedAt, int size) {
        return jpaQueryFactory
                .select(feed.postId)
                .from(feed)
                .where(
                        // ACTIVE 인 feed & (내가 작성한 글 or 다른 유저가 작성한 공개글) & cursorCondition
                        feed.status.eq(StatusType.ACTIVE),
                        feed.userJpaEntity.userId.eq(userId).or(feed.isPublic.eq(true)),
                        lastCreatedAt != null ? feed.createdAt.lt(lastCreatedAt) : Expressions.TRUE
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
                .leftJoin(feed.userJpaEntity, user).fetchJoin()
                .leftJoin(feed.bookJpaEntity, book).fetchJoin()
                .where(feed.postId.in(ids))
                .fetch();
    }

    @Override
    public List<FeedQueryDto> findMyFeedsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size) {
        // 1. 내 피드 ID 목록만 최신순 페이징 조회
        List<Long> feedIds = fetchMyFeedIdsByCreatedAt(userId, lastCreatedAt, size);

        // 2. 엔티티 조회 및 순서 보존
        List<FeedJpaEntity> entities = fetchFeedEntitiesByIds(feedIds);
        Map<Long, FeedJpaEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(FeedJpaEntity::getPostId, e -> e));
        List<FeedJpaEntity> ordered = feedIds.stream()
                .map(entityMap::get)
                .toList();

        // 3) DTO 변환 (priority 없음)
        return ordered.stream()
                .map(e -> toDto(e, null))
                .toList();
    }

    @Override
    public List<FeedQueryDto> findSpecificUserFeedsByCreatedAt(Long feedOwnerId, LocalDateTime lastCreatedAt, int size) {
        // 1. 특정 유저 피드 ID 목록만 최신순 페이징 조회
        List<Long> feedIds = fetchSpecificUserFeedIdsByCreatedAt(feedOwnerId, lastCreatedAt, size);

        // 2. 엔티티 조회 및 순서 보존
        List<FeedJpaEntity> entities = fetchFeedEntitiesByIds(feedIds);
        Map<Long, FeedJpaEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(FeedJpaEntity::getPostId, e -> e));
        List<FeedJpaEntity> ordered = feedIds.stream()
                .map(entityMap::get)
                .toList();

        // 3) DTO 변환 (priority 없음)
        return ordered.stream()
                .map(e -> toDto(e, null))
                .toList();
    }

    private List<Long> fetchMyFeedIdsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size) {
        return jpaQueryFactory
                .select(feed.postId)
                .from(feed)
                .where(
                        // ACTIVE 인 feed & 내가 작성한 글 & cursorCondition
                        feed.status.eq(StatusType.ACTIVE),
                        feed.userJpaEntity.userId.eq(userId),
                        lastCreatedAt != null ? feed.createdAt.lt(lastCreatedAt) : Expressions.TRUE
                )
                .orderBy(feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    private List<Long> fetchSpecificUserFeedIdsByCreatedAt(Long userId, LocalDateTime lastCreatedAt, int size) {
        return jpaQueryFactory
                .select(feed.postId)
                .from(feed)
                .where(
                        // ACTIVE 인 feed & 특정 유저가 작성한 공개 글 & cursorCondition
                        feed.status.eq(StatusType.ACTIVE),
                        feed.userJpaEntity.userId.eq(userId),
                        feed.isPublic.eq(Boolean.TRUE),
                        lastCreatedAt != null ? feed.createdAt.lt(lastCreatedAt) : Expressions.TRUE
                )
                .orderBy(feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    private FeedQueryDto toDto(FeedJpaEntity f, Integer priority) {
        boolean isPriorityFeed = (priority != null && priority == 1);

        return FeedQueryDto.builder()
                .feedId(f.getPostId())
                .creatorId(f.getUserJpaEntity().getUserId())
                .creatorNickname(f.getUserJpaEntity().getNickname())
                .creatorProfileImageUrl(f.getUserJpaEntity().getAlias().getImageUrl())      // TODO : DB에 String alias 만 저장하면 수정해야함
                .alias(f.getUserJpaEntity().getAlias().getValue())
                .createdAt(f.getCreatedAt())
                .isbn(f.getBookJpaEntity().getIsbn())
                .bookTitle(f.getBookJpaEntity().getTitle())
                .bookAuthor(f.getBookJpaEntity().getAuthorName())
                .contentBody(f.getContent())
                .contentUrls(f.getContentList().toArray(String[]::new))
                .likeCount(f.getLikeCount())
                .commentCount(f.getCommentCount())
                .isPublic(f.getIsPublic())
                .isPriorityFeed(isPriorityFeed)
                .build();
    }

    /**
     * 책 ISBN으로 피드를 조회하고, 좋아요 수 기준으로 정렬하여 페이징 처리
     */
    @Override
    public List<FeedQueryDto> findFeedsByBookIsbnOrderByLikeCount(
            String isbn,
            Long userId,
            @Nullable LocalDateTime lastCreatedAt,
            @Nullable Integer lastLikeCount,
            int size
    ) {
        BooleanExpression where = feedByBooksFilter(isbn, userId);
        if (lastLikeCount != null && lastCreatedAt != null) {
            // likeCount DESC → createdAt DESC
            where = where.and(
                    feed.likeCount.lt(lastLikeCount)
                            .or(feed.likeCount.eq(lastLikeCount)
                                    .and(feed.createdAt.lt(lastCreatedAt)))
            );
        }

        return jpaQueryFactory
                .select(toQueryDto())
                .from(feed)
                .join(feed.userJpaEntity, user)
                .join(feed.bookJpaEntity, book)
                .where(where)
                .orderBy(feed.likeCount.desc(), feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * 책 ISBN으로 피드를 조회하고, 최신순 정렬하여 페이징 처리
     */
    @Override
    public List<FeedQueryDto> findFeedsByBookIsbnOrderByCreatedAt(
            String isbn,
            Long userId,
            @Nullable LocalDateTime lastCreatedAt,
            int size
    ) {
        BooleanExpression where = feedByBooksFilter(isbn, userId);
        if (lastCreatedAt != null) {
            // createdAt DESC
            where = where.and(
                    feed.createdAt.lt(lastCreatedAt)
            );
        }

        return jpaQueryFactory
                .select(toQueryDto())
                .from(feed)
                .join(feed.userJpaEntity, user)
                .join(feed.bookJpaEntity, book)
                .where(where)
                .orderBy(feed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    private QFeedQueryDto toQueryDto() {
        return new QFeedQueryDto(
                feed.postId,
                feed.userJpaEntity.userId,
                user.nickname,
                user.alias,
                feed.createdAt,
                book.isbn,
                book.title,
                book.authorName,
                feed.content,
                feed.contentList,
                feed.likeCount,
                feed.commentCount,
                feed.isPublic,
                Expressions.nullExpression(),
                Expressions.nullExpression()
        );
    }

    // 필터링 조건: 책 ISBN과 사용자 ID를 제외한 다른 사용자 공개 피드
    private BooleanExpression feedByBooksFilter(String isbn, Long userId) {
        return feed.status.eq(StatusType.ACTIVE)
                .and(feed.bookJpaEntity.isbn.eq(isbn))
                .and(feed.userJpaEntity.userId.ne(userId))
                .and(feed.isPublic.eq(true));
    }

    @Override
    public List<Long> findLatestPublicFeedCreatorsIn(Set<Long> userIds, int size) {
        return jpaQueryFactory
                .select(feed.userJpaEntity.userId)
                .from(feed)
                .where(
                        feed.userJpaEntity.userId.in(userIds),
                        feed.isPublic.isTrue(),
                        feed.status.eq(StatusType.ACTIVE)
                )
                .groupBy(feed.userJpaEntity.userId)
                .orderBy(feed.createdAt.max().desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<FeedQueryDto> findSavedFeedsByCreatedAt(Long userId, LocalDateTime lastSavedAt, int size) {

        BooleanExpression where = savedFeed.userJpaEntity.userId.eq(userId)
                .and(savedFeed.feedJpaEntity.status.eq(StatusType.ACTIVE))
                .and(
                        savedFeed.feedJpaEntity.userJpaEntity.userId.eq(userId)
                                .or(savedFeed.feedJpaEntity.isPublic.eq(true))
                );

        if (lastSavedAt != null) {
            where = where.and(savedFeed.createdAt.lt(lastSavedAt));
        }

        return jpaQueryFactory
                .select(toSavedFeedQueryDto())
                .from(savedFeed)
                .join(savedFeed.feedJpaEntity, feed)
                .join(feed.userJpaEntity, user)
                .join(feed.bookJpaEntity, book)
                .where(where)
                .orderBy(savedFeed.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    /**
     * SavedFeed 전용 DTO 매핑
     */
    private QFeedQueryDto toSavedFeedQueryDto() {
        return new QFeedQueryDto(
                feed.postId,
                feed.userJpaEntity.userId,
                user.nickname,
                user.alias,
                feed.createdAt,
                book.isbn,
                book.title,
                book.authorName,
                feed.content,
                feed.contentList,
                feed.likeCount,
                feed.commentCount,
                feed.isPublic,
                Expressions.nullExpression(),
                savedFeed.createdAt
        );
    }
}
