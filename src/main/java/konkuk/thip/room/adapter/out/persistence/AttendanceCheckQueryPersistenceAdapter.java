package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.room.adapter.out.persistence.repository.attendanceCheck.AttendanceCheckJpaRepository;
import konkuk.thip.room.application.port.out.AttendanceCheckQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendanceCheckQueryPort {

    private final AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

    @Override
    public int countAttendanceChecksOnTodayByUser(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return attendanceCheckJpaRepository.countByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
    }
}
