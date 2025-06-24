package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserVoteJpaEntity;
import konkuk.thip.user.domain.UserVote;
import org.springframework.stereotype.Component;

@Component
public class UserVoteMapper {

    public UserVoteJpaEntity toJpaEntity(UserJpaEntity userJpaEntity, VoteItemJpaEntity voteItemJpaEntity) {
        return UserVoteJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .voteItemJpaEntity(voteItemJpaEntity)
                .build();
    }

    public UserVote toDomainEntity(UserVoteJpaEntity userVoteJpaEntity) {
        return UserVote.builder()
                .id(userVoteJpaEntity.getUserVoteId())
                .userId(userVoteJpaEntity.getUserJpaEntity().getUserId())
                .voteItemId(userVoteJpaEntity.getVoteItemJpaEntity().getVoteItemId())
                .createdAt(userVoteJpaEntity.getCreatedAt())
                .modifiedAt(userVoteJpaEntity.getModifiedAt())
                .status(userVoteJpaEntity.getStatus())
                .build();
    }
}
