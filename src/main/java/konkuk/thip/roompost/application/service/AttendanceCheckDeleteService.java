package konkuk.thip.roompost.application.service;

import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.application.port.in.AttendanceCheckDeleteUseCase;
import konkuk.thip.roompost.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.roompost.domain.AttendanceCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceCheckDeleteService implements AttendanceCheckDeleteUseCase {

    private final AttendanceCheckCommandPort attendanceCheckCommandPort;
    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    @Transactional
    public Long delete(Long creatorId, Long roomId, Long attendanceCheckId) {
        // 1. creator 가 room participant인지 검증
        roomParticipantValidator.validateUserIsRoomMember(roomId, creatorId);

        // 2. creator 겁증
        AttendanceCheck attendanceCheck = attendanceCheckCommandPort.getByIdOrThrow(attendanceCheckId);
        attendanceCheck.validateCreator(creatorId);

        // 3. 오늘의 한마디 삭제
        attendanceCheckCommandPort.delete(attendanceCheck);
        return roomId;
    }
}
