package konkuk.thip.roompost.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.roompost.domain.VoteItem;
import lombok.*;

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

    /**
     * -- SETTER --
     *  회원 탈퇴용
     */
    @Setter
    @Builder.Default
    @Column(nullable = false)
    private int count = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private VoteJpaEntity voteJpaEntity;

    public VoteItemJpaEntity updateFrom(VoteItem voteItem) {
        this.itemName = voteItem.getItemName();
        this.count = voteItem.getCount();
        return this;
    }
}