package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;


@Entity
@Table(name = "user_votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserVoteJpaEntity extends BaseJpaEntity {

    @EmbeddedId
    private UserVoteJpaEntityId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("voteItemId")
    @JoinColumn(name = "vote_item_id")
    private VoteItemJpaEntity voteItemJpaEntity;
}
