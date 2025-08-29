package konkuk.thip.roompost.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "attendance_checks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE attendance_checks SET status = 'INACTIVE' WHERE attendancecheck_id = ?")
public class AttendanceCheckJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendancecheck_id")
    private Long attendanceCheckId;

    @Column(name = "today_comment",length = 100, nullable = false)
    private String todayComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomJpaEntity roomJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

}
