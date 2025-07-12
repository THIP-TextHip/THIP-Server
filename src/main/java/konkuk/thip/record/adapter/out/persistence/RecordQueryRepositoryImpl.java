package konkuk.thip.record.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.QPostJpaEntity;
import konkuk.thip.record.adapter.in.web.response.RecordDto;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.in.web.response.VoteDto;
import konkuk.thip.record.adapter.out.jpa.QRecordJpaEntity;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecordQueryRepositoryImpl implements RecordQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RecordSearchResponse.RecordSearchResult> findRecordsByRoom(Long roomId, String viewType, Integer pageStart, Integer pageEnd, Boolean isOverview, Long loginUserId, Pageable pageable) {
        QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
        QRecordJpaEntity record = QRecordJpaEntity.recordJpaEntity;
        QVoteJpaEntity vote = QVoteJpaEntity.voteJpaEntity;

        BooleanBuilder where = new BooleanBuilder();
        where.and(post.instanceOf(RecordJpaEntity.class).and(record.roomJpaEntity.roomId.eq(roomId)))
                .or(post.instanceOf(VoteJpaEntity.class).and(vote.roomJpaEntity.roomId.eq(roomId)));

        if (isOverview) {
            where.and(post.instanceOf(RecordJpaEntity.class).and(record.isOverview.isTrue()))
                    .or(post.instanceOf(VoteJpaEntity.class).and(vote.isOverview.isTrue()));
        } else {
            where.and(post.instanceOf(RecordJpaEntity.class).and(record.isOverview.isFalse()))
                    .or(post.instanceOf(VoteJpaEntity.class).and(vote.isOverview.isFalse()));
            where.and(post.instanceOf(RecordJpaEntity.class).and(record.page.between(pageStart, pageEnd)))
                    .or(post.instanceOf(VoteJpaEntity.class).and(vote.page.between(pageStart, pageEnd)));
        }

        if ("mine".equals(viewType)) {
            where.and(post.userJpaEntity.userId.eq(loginUserId));
        }

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            boolean asc = order.getDirection().isAscending();

            if ("likeCount".equalsIgnoreCase(property)) {
                orderSpecifiers.add(new OrderSpecifier<>(asc ? Order.ASC : Order.DESC,
                        record.likeCount.coalesce(0).add(vote.likeCount.coalesce(0))));
            } else if ("commentCount".equalsIgnoreCase(property)) {
                orderSpecifiers.add(new OrderSpecifier<>(asc ? Order.ASC : Order.DESC,
                        record.commentCount.coalesce(0).add(vote.commentCount.coalesce(0))));
            } else if ("createdAt".equalsIgnoreCase(property)) {
                orderSpecifiers.add(asc ? post.createdAt.asc() : post.createdAt.desc());
            } else {
                orderSpecifiers.add(post.createdAt.desc());
            }
        }

        List<PostJpaEntity> posts = queryFactory
                .selectFrom(post)
                .leftJoin(record).on(post.postId.eq(record.postId))
                .leftJoin(vote).on(post.postId.eq(vote.postId))
                .where(where)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<RecordSearchResponse.RecordSearchResult> resultList = posts.stream()
                .map(p -> {
                    if (p instanceof RecordJpaEntity r) {
                        return RecordDto.builder()
                                .postDate(DateUtil.formatBeforeTime(r.getCreatedAt()))
                                .page(r.getPage())
                                .userId(r.getUserJpaEntity().getUserId())
                                .nickName(r.getUserJpaEntity().getNickname())
                                .profileImageUrl(r.getUserJpaEntity().getImageUrl())
                                .content(r.getContent())
                                .likeCount(r.getLikeCount())
                                .commentCount(r.getCommentCount())
                                .isLiked(false) // 초기값은 false, 서비스 레벨에서 처리
                                .isWriter(loginUserId.equals(r.getUserJpaEntity().getUserId()))
                                .recordId(r.getPostId())
                                .build();
                    } else if (p instanceof VoteJpaEntity v) {
                        // VoteItem은 양방향 매핑이 없으므로 빈 리스트로 처리하고 서비스 레벨에서 파싱
                        return VoteDto.builder()
                                .postDate(DateUtil.formatBeforeTime(v.getCreatedAt()))
                                .page(v.getPage())
                                .userId(v.getUserJpaEntity().getUserId())
                                .nickName(v.getUserJpaEntity().getNickname())
                                .profileImageUrl(v.getUserJpaEntity().getImageUrl())
                                .content(v.getContent())
                                .likeCount(v.getLikeCount())
                                .commentCount(v.getCommentCount())
                                .isLiked(false) // 초기값은 false, 서비스 레벨에서 처리
                                .isWriter(loginUserId.equals(v.getUserJpaEntity().getUserId()))
                                .voteId(v.getPostId())
                                .voteItems(new ArrayList<>()) // 빈 리스트로 초기화, 서비스 레벨에서 처리
                                .build();
                    } else {
                        throw new InvalidStateException(ErrorCode.API_SERVER_ERROR, new IllegalStateException("지원되지 않는 게시물 타입: " + p.getClass().getSimpleName()));
                    }
                })
                .map(result -> (RecordSearchResponse.RecordSearchResult) result)
                .toList();

        Long totalCount = queryFactory
                .select(post.count())
                .from(post)
                .leftJoin(record).on(post.postId.eq(record.postId))
                .leftJoin(vote).on(post.postId.eq(vote.postId))
                .where(where)
                .fetchOne();
        long total = (totalCount != null) ? totalCount : 0L;

        return new PageImpl<>(resultList, pageable, total);
    }
}