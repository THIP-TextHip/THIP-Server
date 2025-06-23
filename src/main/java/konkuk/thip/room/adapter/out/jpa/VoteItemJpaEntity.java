package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
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

    @Builder.Default
    @Column(nullable = false)
    private int count = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private VoteJpaEntity voteJpaEntity;

}