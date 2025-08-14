package konkuk.thip.vote.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteItemJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteParticipantJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.application.port.out.dto.QVoteItemQueryDto;
import konkuk.thip.vote.application.port.out.dto.VoteItemQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class VoteQueryRepositoryImpl implements VoteQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private final QVoteJpaEntity vote = QVoteJpaEntity.voteJpaEntity;
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
    private final QVoteItemJpaEntity voteItem = QVoteItemJpaEntity.voteItemJpaEntity;
    private final QVoteParticipantJpaEntity voteParticipant = QVoteParticipantJpaEntity.voteParticipantJpaEntity;

    @Override
    public List<VoteJpaEntity> findVotesByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId) {
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

    @Override
    public List<RoomPlayingDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Long roomId, int count) {
        // 1. Fetch top votes by total participation count
        List<VoteJpaEntity> topVotes = jpaQueryFactory
                .select(vote)
                .from(vote)
                .leftJoin(voteItem).on(voteItem.voteJpaEntity.eq(vote))
                .where(vote.roomJpaEntity.roomId.eq(roomId))
                .groupBy(vote)
                .orderBy(voteItem.count.sum().desc())       // 해당 투표에 참여한 총 참여자 수 기준 내림차순 정렬
                .limit(count)
                .fetch();

        // 2. Map to DTOs including vote items
        return topVotes.stream()
                .map(vote -> {
                    List<RoomPlayingDetailViewResponse.CurrentVote.VoteItem> voteItems = jpaQueryFactory
                            .select(voteItem)
                            .from(voteItem)
                            .where(voteItem.voteJpaEntity.eq(vote))
                            .orderBy(voteItem.count.desc())
                            .fetch()
                            .stream()
                            .map(item -> new RoomPlayingDetailViewResponse.CurrentVote.VoteItem(item.getItemName()))
                            .toList();
                    return new RoomPlayingDetailViewResponse.CurrentVote(
                            vote.getContent(),
                            vote.getPage(),
                            vote.isOverview(),
                            voteItems
                    );
                })
                .toList();
    }

    @Override
    public List<VoteItemQueryDto> mapVoteItemsByVoteIds(Set<Long> voteIds, Long userId) {
        QVoteItemJpaEntity voteItem = QVoteItemJpaEntity.voteItemJpaEntity;
        QVoteParticipantJpaEntity voteParticipant = QVoteParticipantJpaEntity.voteParticipantJpaEntity;

        return jpaQueryFactory
                .select(new QVoteItemQueryDto(
                        voteItem.voteJpaEntity.postId,
                        voteItem.voteItemId,
                        voteItem.itemName,
                        voteItem.count,
                        JPAExpressions
                                .selectOne()
                                .from(voteParticipant)
                                .where(
                                        voteParticipant.voteItemJpaEntity.eq(voteItem),
                                        voteParticipant.userJpaEntity.userId.eq(userId)
                                )
                                .exists() // isVoted : 로그인한 사용자가 해당 투표 아이템에 투표했는지 여부 서브 쿼리
                ))
                .from(voteItem)
                .where(voteItem.voteJpaEntity.postId.in(voteIds))
                .fetch();
    }

    @Override
    public List<VoteItemQueryDto> findVoteItemsByVoteId(Long voteId, Long userId) {
        QVoteItemJpaEntity voteItem = QVoteItemJpaEntity.voteItemJpaEntity;
        QVoteParticipantJpaEntity voteParticipant = QVoteParticipantJpaEntity.voteParticipantJpaEntity;

        return jpaQueryFactory
                .select(new QVoteItemQueryDto(
                        voteItem.voteJpaEntity.postId,
                        voteItem.voteItemId,
                        voteItem.itemName,
                        voteItem.count,
                        JPAExpressions
                                .selectOne()
                                .from(voteParticipant)
                                .where(
                                        voteParticipant.voteItemJpaEntity.eq(voteItem),
                                        voteParticipant.userJpaEntity.userId.eq(userId)
                                )
                                .exists() // isVoted : 로그인한 사용자가 해당 투표 아이템에 투표했는지 여부 서브 쿼리
                ))
                .from(voteItem)
                .where(voteItem.voteJpaEntity.postId.eq(voteId))
                .fetch();
    }
}
