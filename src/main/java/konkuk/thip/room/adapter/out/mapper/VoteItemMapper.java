package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.VoteItemJpaEntity;
import konkuk.thip.room.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.room.domain.VoteItem;
import org.springframework.stereotype.Component;

@Component
public class VoteItemMapper {

    public VoteItemJpaEntity toJpaEntity(VoteItem voteItem, VoteJpaEntity voteJpaEntity) {
        return VoteItemJpaEntity.builder()
                .itemName(voteItem.getItemName())
                .count(voteItem.getCount())
                .voteJpaEntity(voteJpaEntity)
                .build();
    }

    public VoteItem toDomainEntity(VoteItemJpaEntity voteItemJpaEntity) {
        return VoteItem.builder()
                .id(voteItemJpaEntity.getVoteItemId())
                .itemName(voteItemJpaEntity.getItemName())
                .count(voteItemJpaEntity.getCount())
                .voteId(voteItemJpaEntity.getVoteJpaEntity().getPostId())
                .createdAt(voteItemJpaEntity.getCreatedAt())
                .modifiedAt(voteItemJpaEntity.getModifiedAt())
                .status(voteItemJpaEntity.getStatus())
                .build();
    }
}
