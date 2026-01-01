package konkuk.thip.room.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(
        name = "room_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        // TODO : room_participant가 soft delete 된 경우에도 unique 제약조건은 여전히 유효
                        // room_participant가 삭제된 방에 다시 참여하는 경우는 일단 고려 X
                        name = "uk_room_participant_user_room",
                        columnNames = {"user_id", "room_id"}
                )
        }
)
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
    @Column(name = "room_participant_role", nullable = false)
    private RoomParticipantRole roomParticipantRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomJpaEntity roomJpaEntity;

    @VisibleForTesting
    public void updateCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @VisibleForTesting
    public void updateUserPercentage(double userPercentage) {
        this.userPercentage = userPercentage;
    }

    @VisibleForTesting
    public void updateRoleToHost() {
        this.roomParticipantRole = RoomParticipantRole.HOST;
    }

    public void updateFrom(RoomParticipant roomParticipant) {
        this.currentPage = roomParticipant.getCurrentPage();
        this.userPercentage = roomParticipant.getUserPercentage();
        this.roomParticipantRole = RoomParticipantRole.from(roomParticipant.getRoomParticipantRole());
    }
}