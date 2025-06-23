package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import konkuk.thip.domain.room.adapter.out.jpa.RoomJpaEntity;
import lombok.*;

@Entity
@Table(name = "attendance_checks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceCheckJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendancecheck_id")
    private Long attendanceCheckId;

    @Column(name = "today_comment",length = 100, nullable = false)
    private String todayComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

}
