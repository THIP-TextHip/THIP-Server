package konkuk.thip.vote.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class VoteParticipant extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long voteItemId;

    public static VoteParticipant withoutId(Long userId, Long voteItemId) {
        return VoteParticipant.builder()
                .id(null)
                .userId(userId)
                .voteItemId(voteItemId)
                .build();
    }

    public void changeVoteItem(Long voteItemId) {
        // 같은 항목을 투표하려고 하는 경우 예외처리
        if(this.voteItemId.equals(voteItemId)) {
            throw new InvalidStateException(ErrorCode.VOTE_ITEM_ALREADY_VOTED);
        }
        // 투표 항목 변경
        this.voteItemId = voteItemId;
    }
}
