package konkuk.thip.roompost.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;

@Entity
@Table(name = "vote_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoteParticipantJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_participant_id")
    private Long voteParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_item_id", nullable = false)
    private VoteItemJpaEntity voteItemJpaEntity;

    public VoteParticipantJpaEntity updateVoteItem(VoteItemJpaEntity voteItemJpaEntity) {
        this.voteItemJpaEntity = voteItemJpaEntity;
        return this;
    }
}
