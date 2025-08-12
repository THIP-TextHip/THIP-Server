package konkuk.thip.vote.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.domain.Vote;
import org.springframework.stereotype.Component;

@Component
public class VoteMapper {

    public VoteJpaEntity toJpaEntity(Vote vote, UserJpaEntity userJpaEntity, RoomJpaEntity roomJpaEntity) {
        return VoteJpaEntity.builder()
                .content(vote.getContent())
                .userJpaEntity(userJpaEntity)
                .page(vote.getPage())
                .isOverview(vote.isOverview())
                .likeCount(vote.getLikeCount())
                .commentCount(vote.getCommentCount())
                .roomJpaEntity(roomJpaEntity)
                .build();
    }

    public Vote toDomainEntity(VoteJpaEntity voteJpaEntity) {
        return Vote.builder()
                .id(voteJpaEntity.getPostId())
                .content(voteJpaEntity.getContent())
                .creatorId(voteJpaEntity.getUserJpaEntity().getUserId())
                .page(voteJpaEntity.getPage())
                .isOverview(voteJpaEntity.isOverview())
                .likeCount(voteJpaEntity.getLikeCount())
                .commentCount(voteJpaEntity.getCommentCount())
                .roomId(voteJpaEntity.getRoomJpaEntity().getRoomId())
                .createdAt(voteJpaEntity.getCreatedAt())
                .modifiedAt(voteJpaEntity.getModifiedAt())
                .status(voteJpaEntity.getStatus())
                .build();
    }
}
