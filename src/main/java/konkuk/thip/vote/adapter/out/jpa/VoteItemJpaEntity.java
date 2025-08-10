package konkuk.thip.vote.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.vote.domain.VoteItem;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vote_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoteItemJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_item_id")
    private Long voteItemId;

    @Column(name = "item_name",length = 70, nullable = false)
    private String itemName;

    @Builder.Default
    @Column(nullable = false)
    private int count = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private VoteJpaEntity voteJpaEntity;

    // 삭제용 투표 참여자 양방향 매핑 관계
    @OneToMany(mappedBy = "voteItemJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteParticipantJpaEntity> voteParticipants = new ArrayList<>();

    public VoteItemJpaEntity updateFrom(VoteItem voteItem) {
        this.itemName = voteItem.getItemName();
        this.count = voteItem.getCount();
        return this;
    }
}