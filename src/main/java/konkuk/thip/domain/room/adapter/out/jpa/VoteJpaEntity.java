package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(name = "votes")
@DiscriminatorValue("VOTE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteJpaEntity extends PostJpaEntity {

    private Integer page;

    @Column(name = "is_overview",nullable = false)
    private boolean isOverview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;

    @Builder
    public VoteJpaEntity(String content, UserJpaEntity userJpaEntity, Integer page, boolean isOverview, RoomJpaEntity roomJpaEntity) {
        super(content, userJpaEntity);
        this.page = page;
        this.isOverview = isOverview;
        this.roomJpaEntity = roomJpaEntity;
    }
}