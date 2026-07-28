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

import java.util.*;
import java.util.stream.Collectors;

import static konkuk.thip.common.entity.StatusType.ACTIVE;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    /**
     * 댓글 관련 queryDsl 코드에서는 status 값 명시해야함 (서비스 메서드에서 status filter off가 전제)
     */

    private final JPAQueryFactory queryFactory;

    private final QCommentJpaEntity comment = QCommentJpaEntity.commentJpaEntity;
    private final QUserJpaEntity commentCreator = QUserJpaEntity.userJpaEntity;
    private final QCommentJpaEntity parentComment = new QCommentJpaEntity("parentComment");
    private final QUserJpaEntity parentCommentCreator = new QUserJpaEntity("parentCommentCreator");

    // 최상위 댓글 조회 (삭제된 댓글 포함, 최신순, 페이징)
    @Override
    public List<CommentQueryDto> findRootCommentsWithDeletedByCreatedAtDesc(Long postId, Long lastRootCommentId, int size) {
        // 최상위 댓글(size+1) 프로젝션 생성
        QCommentQueryDto proj = new QCommentQueryDto(
                comment.commentId,
                commentCreator.userId,
                commentCreator.alias,
                commentCreator.nickname,
                comment.createdAt,
                comment.content,
                comment.likeCount,
                comment.status.eq(StatusType.INACTIVE),      // 루트 댓글이 삭제된 상태인지 아닌지
                comment.descendantCount  // 자식 댓글 수
        );

        // WHERE 절 분리
        BooleanExpression whereClause = comment.postJpaEntity.postId.eq(postId)
                .and(comment.parent.isNull())       // 게시글의 최상위 댓글 조회
                .and(commentCreator.status.eq(ACTIVE))  // 댓글 작성자 ACTIVE
                .and(lastRootCommentId != null      // 최신순 정렬
                        ? comment.commentId.lt(lastRootCommentId)
                        : Expressions.TRUE
                );

        // 조회 및 반환
        return queryFactory
                .select(proj)
                .from(comment)
                .join(comment.userJpaEntity, commentCreator)
                .where(whereClause)
                .orderBy(comment.commentId.desc())
                .limit(size + 1)        // size + 1 개 조회
                .fetch();
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
                comment.status.eq(StatusType.INACTIVE),
                comment.descendantCount  // 자식 댓글 수
        );

        return queryFactory
                .select(proj)
                .from(comment)
                .join(comment.userJpaEntity, commentCreator)
                .where(
                        comment.commentId.eq(rootCommentId),
                        comment.status.eq(ACTIVE)
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
                        parentComment.status.eq(ACTIVE),
                        comment.status.eq(ACTIVE),
                        comment.commentId.eq(replyCommentId)
                )
                .fetchOne();
    }

    @Override
    public List<CommentQueryDto> findAllDescendantCommentsByCreatedAtAsc(Long rootCommentId, Long lastCommentId, int size) {
        // 자손 댓글용 프로젝션 (부모 댓글 ID·작성자 닉네임 포함)
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

        // WHERE 절: root_comment_id 기반
        BooleanExpression whereClause = comment.root.commentId.eq(rootCommentId)
                .and(lastCommentId != null
                        ? comment.commentId.gt(lastCommentId)
                        : Expressions.TRUE
                );

        // 쿼리: 한 번에 끝!
        return queryFactory
                .select(proj)
                .from(comment)
                .leftJoin(comment.parent, parentComment)
                .leftJoin(parentComment.userJpaEntity, parentCommentCreator)
                .join(comment.userJpaEntity, commentCreator)
                .where(whereClause)
                .orderBy(comment.commentId.asc())
                .limit(size + 1)
                .fetch();
    }
}
