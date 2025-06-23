package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;


@Entity
@Table(name = "records")
@DiscriminatorValue("RECORD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordJpaEntity extends PostJpaEntity {
    private Integer page;

    @Column(name = "is_overview",nullable = false)
    private boolean isOverview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;

    @Builder
    public RecordJpaEntity(String content, UserJpaEntity userJpaEntity, Integer page, boolean isOverview, RoomJpaEntity roomJpaEntity) {
        super(content, userJpaEntity);
        this.page = page;
        this.isOverview = isOverview;
        this.roomJpaEntity = roomJpaEntity;
    }
}

