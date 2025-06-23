package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(name = "user_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRoomJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userroom_id")
    private Long userRoomId;

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
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;
}