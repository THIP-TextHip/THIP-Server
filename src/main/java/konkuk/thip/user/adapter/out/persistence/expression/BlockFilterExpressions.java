package konkuk.thip.user.adapter.out.persistence.expression;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import konkuk.thip.user.adapter.out.jpa.QUserBlockJpaEntity;

// 차단한 사용자의 콘텐츠를 목록에서 숨기기 위한 QueryDSL 조건. 차단은 양방향으로 적용된다.
public final class BlockFilterExpressions {

    // 바깥 쿼리와의 별칭 충돌 방지
    private static final QUserBlockJpaEntity BLOCK = new QUserBlockJpaEntity("blockFilter");

    private BlockFilterExpressions() {
    }

    // viewerId 가 null 이면 조건을 걸지 않는다 (Querydsl 에서 null 조건은 무시됨)
    public static BooleanExpression notBlockedWith(NumberPath<Long> authorUserIdPath, Long viewerId) {
        if (viewerId == null) {
            return null;
        }
        return blockedPairExists(authorUserIdPath, viewerId).not();
    }

    // 내가 상대를 차단했거나, 상대가 나를 차단한 경우
    private static BooleanExpression blockedPairExists(NumberPath<Long> otherUserIdPath, Long viewerId) {
        return JPAExpressions
                .selectOne()
                .from(BLOCK)
                .where(
                        BLOCK.userJpaEntity.userId.eq(viewerId)
                                .and(BLOCK.blockedUserJpaEntity.userId.eq(otherUserIdPath))
                                .or(BLOCK.userJpaEntity.userId.eq(otherUserIdPath)
                                        .and(BLOCK.blockedUserJpaEntity.userId.eq(viewerId)))
                )
                .exists();
    }
}
