package konkuk.thip.feed.adapter.out.persistence.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import konkuk.thip.book.adapter.out.jpa.QBookJpaEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.feed.adapter.out.jpa.*;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.application.port.out.dto.QFeedQueryDto;
import konkuk.thip.feed.application.port.out.dto.QTagCategoryQueryDto;
import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import konkuk.thip.room.adapter.out.jpa.QCategoryJpaEntity;
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
                .map(e -> toDto(e, priorityMap.get(e.getPostId()),null))
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
                .map(e -> toDto(e, null,null))
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
                .leftJoin(feed.contentList, content).fetchJoin()
                .leftJoin(feed.userJpaEntity, user).fetchJoin()
                .leftJoin(user.aliasForUserJpaEntity, alias).fetchJoin()
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
                .map(e -> toDto(e, null, null))
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
                .map(e -> toDto(e, null, null))
                .toList();
    }

    @Override
    public List<TagCategoryQueryDto> findAllTags() {
        QCategoryJpaEntity c = QCategoryJpaEntity.categoryJpaEntity;
        QTagJpaEntity t = QTagJpaEntity.tagJpaEntity;

        return jpaQueryFactory
                .select(new QTagCategoryQueryDto(c.value, t.value))
                .from(c)
                .join(t).on(t.categoryJpaEntity.eq(c))
                .orderBy(c.categoryId.asc(), t.tagId.asc()) //Id 순 정렬
                .fetch();
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

    private FeedQueryDto toDto(FeedJpaEntity e, Integer priority, LocalDateTime savedCreatedAt) {
        String[] urls = e.getContentList().stream()
                .map(ContentJpaEntity::getContentUrl)
                .toArray(String[]::new);
        boolean isPriorityFeed = (priority != null && priority == 1);

        return FeedQueryDto.builder()
                .feedId(e.getPostId())
                .creatorId(e.getUserJpaEntity().getUserId())
                .creatorNickname(e.getUserJpaEntity().getNickname())
                .creatorProfileImageUrl(e.getUserJpaEntity().getAliasForUserJpaEntity().getImageUrl())      // TODO : DB에 String alias 만 저장하면 수정해야함
                .alias(e.getUserJpaEntity().getAliasForUserJpaEntity().getValue())
                .createdAt(e.getCreatedAt())
                .isbn(e.getBookJpaEntity().getIsbn())
                .bookTitle(e.getBookJpaEntity().getTitle())
                .bookAuthor(e.getBookJpaEntity().getAuthorName())
                .contentBody(e.getContent())
                .contentUrls(urls)
                .likeCount(e.getLikeCount())
                .commentCount(e.getCommentCount())
                .isPublic(e.getIsPublic())
                .isPriorityFeed(isPriorityFeed)
                .savedCreatedAt(savedCreatedAt)
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
                user.aliasForUserJpaEntity.imageUrl,
                user.aliasForUserJpaEntity.value,
                feed.createdAt,
                book.isbn,
                book.title,
                book.authorName,
                feed.content,
                // 서브쿼리로 N:1 방지
                JPAExpressions
                        .select(contentUrlAggExpr())
                        .from(content)
                        .where(content.postJpaEntity.postId.eq(feed.postId)),
                feed.likeCount,
                feed.commentCount,
                feed.isPublic,
                Expressions.nullExpression(),
                Expressions.nullExpression()
        );
    }

    // contentUrl을 GROUP_CONCAT으로 묶어서 반환하는 표현식
    private StringExpression contentUrlAggExpr() {
        return Expressions.stringTemplate(
                "group_concat({0})",
                content.contentUrl
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

        // 1. SavedFeed를 한 번에 페이징 조회하며 feed와 연관 엔티티 fetch join
        List<SavedFeedJpaEntity> savedFeeds = getSavedFeedJpaEntities(userId, lastSavedAt, size);
        if (savedFeeds.isEmpty()) {
            return List.of();
        }

        // 2. 저장순대로 FeedQueryDto 변환 (Feed 및 savedCreatedAt 정보 포함)
        return savedFeeds.stream()
                .map(saved -> toDto(saved.getFeedJpaEntity(), null, saved.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private List<SavedFeedJpaEntity> getSavedFeedJpaEntities(Long userId, LocalDateTime lastSavedAt, int size) {
        List<SavedFeedJpaEntity> savedFeeds = jpaQueryFactory
                .selectFrom(savedFeed)
                .leftJoin(savedFeed.feedJpaEntity, feed).fetchJoin()
                .leftJoin(feed.contentList, content).fetchJoin()
                .leftJoin(feed.userJpaEntity, user).fetchJoin()
                .leftJoin(user.aliasForUserJpaEntity, alias).fetchJoin()
                .leftJoin(feed.bookJpaEntity, book).fetchJoin()
                .where(
                        savedFeed.userJpaEntity.userId.eq(userId),
                        savedFeed.feedJpaEntity.status.eq(StatusType.ACTIVE),
                        savedFeed.feedJpaEntity.userJpaEntity.userId.eq(userId)
                                .or(savedFeed.feedJpaEntity.isPublic.eq(true)),
                        lastSavedAt != null ? savedFeed.createdAt.lt(lastSavedAt) : Expressions.TRUE
                )
                .orderBy(savedFeed.createdAt.desc())
                .limit(size + 1)
                .fetch();
        return savedFeeds;
    }
}
