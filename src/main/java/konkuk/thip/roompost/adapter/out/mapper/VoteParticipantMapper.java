package konkuk.thip.roompost.adapter.out.mapper;

import konkuk.thip.roompost.domain.VoteParticipant;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteParticipantJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.VoteItemJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VoteParticipantMapper {

    public VoteParticipantJpaEntity toJpaEntity(UserJpaEntity userJpaEntity, VoteItemJpaEntity voteItemJpaEntity) {
        return VoteParticipantJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .voteItemJpaEntity(voteItemJpaEntity)
                .build();
    }

    public VoteParticipant toDomainEntity(VoteParticipantJpaEntity voteParticipantJpaEntity) {
        return VoteParticipant.builder()
                .id(voteParticipantJpaEntity.getVoteParticipantId())
                .userId(voteParticipantJpaEntity.getUserJpaEntity().getUserId())
                .voteItemId(voteParticipantJpaEntity.getVoteItemJpaEntity().getVoteItemId())
                .createdAt(voteParticipantJpaEntity.getCreatedAt())
                .modifiedAt(voteParticipantJpaEntity.getModifiedAt())
                .status(voteParticipantJpaEntity.getStatus())
                .build();
    }
}
