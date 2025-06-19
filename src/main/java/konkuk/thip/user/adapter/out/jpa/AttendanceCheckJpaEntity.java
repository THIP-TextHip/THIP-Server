package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import lombok.*;

@Entity
@Table(name = "attendance_checks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceCheckJpaEntity extends BaseJpaEntity {

    @EmbeddedId
    private AttendanceCheckJpaEntityId id;

    @Column(name = "today_comment",length = 100, nullable = false)
    private String todayComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

}
