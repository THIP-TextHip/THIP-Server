package konkuk.thip.roompost.application.service;

import konkuk.thip.roompost.application.port.in.AttendanceCheckCreateUseCase;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateResult;
import konkuk.thip.roompost.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.roompost.application.port.out.AttendanceCheckQueryPort;
import konkuk.thip.roompost.domain.AttendanceCheck;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceCheckCreateService implements AttendanceCheckCreateUseCase {

    private final RoomParticipantValidator roomParticipantValidator;
    private final AttendanceCheckCommandPort attendanceCheckCommandPort;
    private final AttendanceCheckQueryPort attendanceCheckQueryPort;

    @Transactional
    @Override
    public AttendanceCheckCreateResult create(AttendanceCheckCreateCommand command) {
        // 1. 유저가 해당 방에 오늘의 한마디를 작성할 수 있는지 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.creatorId());

        // 2. 유저가 해당 방에서 오늘 이미 작성한 오늘의 한마디 개수 조회
        int alreadyWrittenCountToday = attendanceCheckQueryPort.countAttendanceChecksOnTodayByUser(command.creatorId(), command.roomId());

        // 3. 출석체크 도메인 생성 및 저장
        AttendanceCheck attendanceCheck = AttendanceCheck.withoutId(command.roomId(), command.creatorId(), command.content(), alreadyWrittenCountToday);
        Long savedId = attendanceCheckCommandPort.save(attendanceCheck);

        return AttendanceCheckCreateResult.builder()
                .attendanceCheckId(savedId)
                .roomId(command.roomId())
                .todayWriteCountOfUser(alreadyWrittenCountToday + 1)
                .build();
    }
}
