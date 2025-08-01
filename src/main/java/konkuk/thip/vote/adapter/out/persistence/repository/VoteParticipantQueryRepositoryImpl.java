package konkuk.thip.vote.adapter.out.persistence.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.vote.adapter.out.jpa.QVoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteParticipantJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteParticipantJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.entity.StatusType.ACTIVE;

@Repository
@RequiredArgsConstructor
public class VoteParticipantQueryRepositoryImpl implements VoteParticipantQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final QVoteParticipantJpaEntity voteParticipant = QVoteParticipantJpaEntity.voteParticipantJpaEntity;
    private final QVoteItemJpaEntity voteItem = QVoteItemJpaEntity.voteItemJpaEntity;
    private final QVoteJpaEntity vote = QVoteJpaEntity.voteJpaEntity;

    /**
     * 사용자가 해당 투표에서 어떤 투표 항목에 투표했는지 확인하는 쿼리
     */
    @Override
    public Optional<VoteParticipantJpaEntity> findVoteParticipantByUserIdAndVoteId(Long userId, Long voteId) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(voteParticipant)
                .join(voteParticipant.voteItemJpaEntity, voteItem).fetchJoin()
                .join(voteItem.voteJpaEntity, vote).fetchJoin()
                .where(
                        voteParticipant.userJpaEntity.userId.eq(userId),
                        vote.postId.eq(voteId),
                        vote.status.eq(ACTIVE)
                )
                .fetchOne());

    }
}
