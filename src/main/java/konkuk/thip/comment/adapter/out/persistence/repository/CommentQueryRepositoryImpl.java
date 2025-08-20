package konkuk.thip.comment.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.comment.adapter.out.jpa.QCommentJpaEntity;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.comment.application.port.out.dto.QCommentQueryDto;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QCommentJpaEntity comment = QCommentJpaEntity.commentJpaEntity;
    private final QUserJpaEntity commentCreator = QUserJpaEntity.userJpaEntity;
    private final QCommentJpaEntity parentComment = new QCommentJpaEntity("parentComment");
    private final QUserJpaEntity parentCommentCreator = new QUserJpaEntity("parentCommentCreator");

    @Override
    public List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, String postTypeStr, LocalDateTime lastCreatedAt, int size) {
        // 최상위 댓글(size+1) 프로젝션 생성
        QCommentQueryDto proj = new QCommentQueryDto(
                comment.commentId,
                commentCreator.userId,
                commentCreator.alias,
                commentCreator.nickname,
                comment.createdAt,
                comment.content,
                comment.likeCount,
                comment.status.eq(StatusType.INACTIVE)      // 루트 댓글이 삭제된 상태인지 아닌지
        );

        // WHERE 절 분리
        BooleanExpression whereClause = comment.postJpaEntity.postId.eq(postId)
                .and(comment.postJpaEntity.dtype.eq(postTypeStr))       // dType 필터링 추가
                .and(comment.parent.isNull())       // 게시글의 최상위 댓글 조회
                .and(lastCreatedAt != null      // 최신순 정렬
                        ? comment.createdAt.lt(lastCreatedAt)
                        : Expressions.TRUE
                );

        // 조회 및 반환
        return queryFactory
                .select(proj)
                .from(comment)
                .leftJoin(comment.userJpaEntity, commentCreator)
                .where(whereClause)
                .orderBy(comment.createdAt.desc())
                .limit(size + 1)        // size + 1 개 조회
                .fetch();
    }

    @Override
    public List<CommentQueryDto> findAllActiveChildCommentsByCreatedAtAsc(Long rootCommentId) {
        List<CommentQueryDto> allDescendants = new ArrayList<>();       // 결과 누적용 리스트

        // 1) 부모 ID 집합에 루트 댓글 ID 추가
        Set<Long> parentIds = new HashSet<>();
        parentIds.add(rootCommentId);

        // 2) 자손 댓글용 프로젝션 (부모 댓글 ID·작성자 닉네임 포함)
        QCommentQueryDto childProj = new QCommentQueryDto(
                comment.commentId,
                comment.parent.commentId,
                parentCommentCreator.nickname,
                commentCreator.userId,
                commentCreator.alias,
                commentCreator.nickname,
                comment.createdAt,
                comment.content,
                comment.likeCount,
                comment.status.eq(StatusType.INACTIVE)
        );

        // 3) 단계별 자식 댓글 조회
        while (!parentIds.isEmpty()) {
            List<CommentQueryDto> children = queryFactory
                    .select(childProj)
                    .from(comment)
                    .leftJoin(comment.parent, parentComment)
                    .leftJoin(parentComment.userJpaEntity, parentCommentCreator)
                    .leftJoin(comment.userJpaEntity, commentCreator)
                    .where(
                            comment.parent.commentId.in(parentIds),     // parentIds 하위의 모든 자식 댓글 조회
                            comment.status.eq(StatusType.ACTIVE)        // 자식 댓글은 ACTIVE인 것만 조회
                    )
                    .fetch();

            if (children.isEmpty()) break;

            // 4) 누적 및 다음 단계 부모 ID 집합 갱신
            allDescendants.addAll(children);
            parentIds = children.stream()
                    .map(CommentQueryDto::commentId)
                    .collect(Collectors.toSet());
        }

        // 5) 전체 자손 댓글을 깊이와 상관없이 작성 순으로 재정렬
        allDescendants.sort(Comparator.comparing(CommentQueryDto::createdAt));
        return allDescendants;
    }

    @Override
    public Map<Long, List<CommentQueryDto>> findAllActiveChildCommentsByCreatedAtAsc(Set<Long> rootCommentIds) {
        // 1) 루트 ID별로 최상위 매핑 초기화
        Map<Long, Long> idToRoot = new HashMap<>();
        for (Long rootId : rootCommentIds) {
            idToRoot.put(rootId, rootId);       // 초기화
        }

        // 2) 결과 맵 초기화
        Map<Long, List<CommentQueryDto>> resultMap = new HashMap<>();
        for (Long rootId : rootCommentIds) {
            resultMap.put(rootId, new ArrayList<>());
        }

        // 3) 단계별 조회용 parentIds 초기화
        Set<Long> parentIds = new HashSet<>(rootCommentIds);

        // 4) 자손 댓글용 프로젝션 정의
        QCommentQueryDto childProj = new QCommentQueryDto(
                comment.commentId,
                comment.parent.commentId,
                parentCommentCreator.nickname,
                commentCreator.userId,
                commentCreator.alias,
                commentCreator.nickname,
                comment.createdAt,
                comment.content,
                comment.likeCount,
                comment.status.eq(StatusType.INACTIVE)
        );

        // 5) 루프를 돌며 모든 깊이의 자식 댓글 조회 및 매핑
        while (!parentIds.isEmpty()) {
            List<CommentQueryDto> children = queryFactory
                    .select(childProj)
                    .from(comment)
                    .leftJoin(comment.parent, parentComment)
                    .leftJoin(parentComment.userJpaEntity, parentCommentCreator)
                    .leftJoin(comment.userJpaEntity, commentCreator)
                    .where(
                            comment.parent.commentId.in(parentIds),     // parentIds 하위의 모든 자식 댓글 조회
                            comment.status.eq(StatusType.ACTIVE)        // 자식 댓글은 ACTIVE인 것만 조회
                    )
                    .fetch();

            if (children.isEmpty()) break;

            Set<Long> nextParentIds = new HashSet<>();
            for (CommentQueryDto child : children) {    // 조회한 자식 댓글들에 대하여
                Long rootId = idToRoot.get(child.parentCommentId());    // 현재 자식댓글의 루트 댓글(부모 아님, 루트임)

                resultMap.get(rootId).add(child);   // 해당 루트 ID의 리스트에 자식 댓글 추가

                // 현재 자식 댓글도 다음 단계의 parentIds로 사용하기 위해 매핑 저장
                idToRoot.put(child.commentId(), rootId);
                nextParentIds.add(child.commentId());
            }
            parentIds = nextParentIds;  // 한단계 아래 계층에서 활용할 부모 댓글들
        }

        // 6) 각 루트별 value 리스트를 작성시간순으로 정렬
        resultMap.values().forEach(list -> list.sort(Comparator.comparing(CommentQueryDto::createdAt)));

        return resultMap;
    }

    @Override
    public CommentQueryDto findRootCommentId(Long rootCommentId) {

        QCommentQueryDto proj = new QCommentQueryDto(
                comment.commentId,
                commentCreator.userId,
                commentCreator.alias,
                commentCreator.nickname,
                comment.createdAt,
                comment.content,
                comment.likeCount,
                comment.status.eq(StatusType.INACTIVE)
        );

        return queryFactory
                .select(proj)
                .from(comment)
                .join(comment.userJpaEntity, commentCreator)
                .where(
                        comment.commentId.eq(rootCommentId),
                        comment.status.eq(StatusType.ACTIVE)
                )
                .fetchOne();
    }

    @Override
    public CommentQueryDto findChildCommentId(Long rootCommentId, Long replyCommentId) {

        QCommentQueryDto proj = new QCommentQueryDto(
                comment.commentId,
                comment.parent.commentId,
                parentCommentCreator.nickname,
                commentCreator.userId,
                commentCreator.alias,
                commentCreator.nickname,
                comment.createdAt,
                comment.content,
                comment.likeCount,
                comment.status.eq(StatusType.INACTIVE)
        );

        return queryFactory
                .select(proj)
                .from(comment)
                .join(comment.parent, parentComment)
                .join(parentComment.userJpaEntity, parentCommentCreator)
                .join(comment.userJpaEntity, commentCreator)
                .where(
                        comment.parent.commentId.eq(rootCommentId),
                        parentComment.status.eq(StatusType.ACTIVE),
                        comment.status.eq(StatusType.ACTIVE),
                        comment.commentId.eq(replyCommentId)
                )
                .fetchOne();
    }
}
