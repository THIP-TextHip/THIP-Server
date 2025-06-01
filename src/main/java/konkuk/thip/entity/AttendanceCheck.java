package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "attendance_check")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceCheck extends BaseEntity {

    @EmbeddedId
    private AttendanceCheckId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "today_comment",length = 100, nullable = false)
    private String todayComment;

}
