package konkuk.thip.roompost.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.roompost.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.roompost.application.port.out.AttendanceCheckQueryPort;
import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendanceCheckQueryPort {

    private final AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

    @Override
    public int countAttendanceChecksOnTodayByUser(Long userId, Long roomId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return attendanceCheckJpaRepository.countByUserIdAndRoomIdAndCreatedAtBetween(userId, roomId, startOfDay, endOfDay);
    }

    @Override
    public CursorBasedList<AttendanceCheckQueryDto> findAttendanceChecksByCreatedAtDesc(Long roomId, Cursor cursor, Long viewerId) {
        LocalDateTime lastCreateAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int size = cursor.getPageSize();

        List<AttendanceCheckQueryDto> attendanceCheckQueryDtos = attendanceCheckJpaRepository.findAttendanceChecksByCreatedAtDesc(roomId, lastCreateAt, size, viewerId);

        return CursorBasedList.of(attendanceCheckQueryDtos, size, attendanceCheckQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(attendanceCheckQueryDto.createdAt().toString()));
            return nextCursor.toEncodedString();
        });
    }
}
