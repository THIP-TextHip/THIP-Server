package konkuk.thip.roompost.adapter.out.persistence;

import konkuk.thip.roompost.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck.AttendanceCheckJpaRepository;
import konkuk.thip.roompost.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.roompost.domain.AttendanceCheck;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckCommandPersistenceAdapter implements AttendanceCheckCommandPort {

    private final AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    private final RoomJpaRepository roomJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

    @Override
    public Long save(AttendanceCheck attendanceCheck) {
        RoomJpaEntity roomJpaEntity = roomJpaRepository.findByRoomId(attendanceCheck.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(attendanceCheck.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        return attendanceCheckJpaRepository.save(
                attendanceCheckMapper.toJpaEntity(attendanceCheck, roomJpaEntity, userJpaEntity)
        ).getAttendanceCheckId();
    }

    @Override
    public Optional<AttendanceCheck> findById(Long id) {
        return attendanceCheckJpaRepository.findByAttendanceCheckId(id)
                .map(attendanceCheckMapper::toDomainEntity);
    }
}
