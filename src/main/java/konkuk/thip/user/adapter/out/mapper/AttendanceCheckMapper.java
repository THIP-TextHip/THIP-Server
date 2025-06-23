package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.AttendanceCheckJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.AttendanceCheck;
import org.springframework.stereotype.Component;

@Component
public class AttendanceCheckMapper {

    public AttendanceCheckJpaEntity toJpaEntity(AttendanceCheck attendanceCheck, RoomJpaEntity roomJpaEntity, UserJpaEntity userJpaEntity) {
        return AttendanceCheckJpaEntity.builder()
                .todayComment(attendanceCheck.getTodayComment())
                .roomJpaEntity(roomJpaEntity)
                .userJpaEntity(userJpaEntity)
                .build();
    }

    public AttendanceCheck toDomainEntity(AttendanceCheckJpaEntity attendanceCheckJpaEntity) {
        return AttendanceCheck.builder()
                .id(attendanceCheckJpaEntity.getAttendanceCheckId())
                .todayComment(attendanceCheckJpaEntity.getTodayComment())
                .roomId(attendanceCheckJpaEntity.getRoomJpaEntity().getRoomId())
                .creatorId(attendanceCheckJpaEntity.getUserJpaEntity().getUserId())
                .createdAt(attendanceCheckJpaEntity.getCreatedAt())
                .modifiedAt(attendanceCheckJpaEntity.getModifiedAt())
                .status(attendanceCheckJpaEntity.getStatus())
                .build();
    }

}
