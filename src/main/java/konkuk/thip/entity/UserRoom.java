package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "user_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserRoom extends BaseEntity {

    @EmbeddedId
    private UserRoomId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "current_page",nullable = false)
    private int currentPage;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role",nullable = false)
    private UserRole userRole;

    @Column(name = "user_percentage",nullable = false)
    private double userPercentage;
}