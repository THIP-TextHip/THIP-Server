package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(name = "user_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRoomJpaEntity extends BaseJpaEntity {

    @EmbeddedId
    private UserRoomJpaEntityId id;

    @Builder.Default
    @Column(name = "current_page",nullable = false)
    private int currentPage = 0;

    @Builder.Default
    @Column(name = "user_percentage",nullable = false)
    private double userPercentage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role",nullable = false)
    private UserRoomRole userRoomRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;
}