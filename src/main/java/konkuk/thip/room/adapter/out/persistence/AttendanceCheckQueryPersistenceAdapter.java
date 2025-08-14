package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.room.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.room.application.port.out.AttendanceCheckQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static konkuk.thip.common.entity.StatusType.ACTIVE;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendanceCheckQueryPort {

    private final AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

    @Override
    public int countAttendanceChecksOnTodayByUser(Long userId, Long roomId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return attendanceCheckJpaRepository.countByUserIdAndRoomIdAndCreatedAtBetween(userId, roomId, startOfDay, endOfDay, ACTIVE);
    }
}
