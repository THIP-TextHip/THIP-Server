package konkuk.thip.vote.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VoteQueryRepositoryImpl implements VoteQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<VoteJpaEntity> findVotesByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId) {
        QVoteJpaEntity vote = QVoteJpaEntity.voteJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

        return jpaQueryFactory
                .select(vote)
                .from(vote)
                .leftJoin(vote.userJpaEntity, user).fetchJoin()
                .where(
                        vote.roomJpaEntity.roomId.eq(roomId),
                        filterByType(type, vote, userId),
                        (startEndNull(pageStart, pageEnd) ? vote.isOverview.isTrue() : vote.page.between(pageStart, pageEnd))
                )
                .fetch();
    }

    private boolean startEndNull(Integer start, Integer end) {
        return start == null && end == null;
    }

    private BooleanExpression filterByType(String type, QVoteJpaEntity post, Long userId) {
        if ("mine".equalsIgnoreCase(type)) {
            return post.userJpaEntity.userId.eq(userId);
        }
        return null;
    }
}
