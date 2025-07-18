package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "room_participants")
@Getter
@SQLDelete(sql = "UPDATE room_participants SET status = 'INACTIVE' WHERE room_participant_id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomParticipantJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_participant_id")
    private Long roomParticipantId;

    @Builder.Default
    @Column(name = "current_page",nullable = false)
    private int currentPage = 0;

    @Builder.Default
    @Column(name = "user_percentage",nullable = false)
    private double userPercentage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_participant_role",nullable = false)
    private RoomParticipantRole roomParticipantRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;
}