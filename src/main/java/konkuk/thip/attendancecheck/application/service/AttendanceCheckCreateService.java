package konkuk.thip.attendancecheck.application.service;

import konkuk.thip.attendancecheck.application.port.in.AttendanceCheckCreateUseCase;
import konkuk.thip.attendancecheck.application.port.in.dto.AttendanceCheckCreateCommand;
import konkuk.thip.attendancecheck.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.attendancecheck.domain.AttendanceCheck;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceCheckCreateService implements AttendanceCheckCreateUseCase {

    private final RoomParticipantValidator roomParticipantValidator;
    private final AttendanceCheckCommandPort attendanceCheckCommandPort;

    @Transactional
    @Override
    public Long create(AttendanceCheckCreateCommand command) {
        // 1. 유저가 해당 방에 오늘의 한마디를 작성할 수 있는지 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.creatorId());

        // 2. 출석체크 도메인 생성 및 저장
        AttendanceCheck attendanceCheck = AttendanceCheck.withoutId(command.roomId(), command.creatorId(), command.content());
        return attendanceCheckCommandPort.save(attendanceCheck);
    }
}
