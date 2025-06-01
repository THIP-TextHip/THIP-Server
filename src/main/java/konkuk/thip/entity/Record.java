package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseEntity;
import lombok.*;


@Entity
@Table(name = "records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Record extends BaseEntity {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private Integer page;

    @Column(name = "is_overview",nullable = false)
    private boolean isOverview;
}

