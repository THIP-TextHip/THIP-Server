package konkuk.thip.roompost.application.service;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.roompost.application.port.in.AttendanceCheckCreateUseCase;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateResult;
import konkuk.thip.roompost.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.roompost.application.port.out.AttendanceCheckQueryPort;
import konkuk.thip.roompost.domain.AttendanceCheck;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceCheckCreateService implements AttendanceCheckCreateUseCase {

    private final RoomParticipantValidator roomParticipantValidator;
    private final AttendanceCheckCommandPort attendanceCheckCommandPort;
    private final UserCommandPort userCommandPort;
    private final AttendanceCheckQueryPort attendanceCheckQueryPort;
    private final RoomCommandPort roomCommandPort;

    @Transactional
    @Override
    public AttendanceCheckCreateResult create(AttendanceCheckCreateCommand command) {

        // 1. 유저 검증 및 조회
        User user = userCommandPort.findById(command.creatorId());
        // 1-1. 유저가 해당 방에 오늘의 한마디를 작성할 수 있는지 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.creatorId());

        // 1-2. 방이 만료되었는지 검증
        Room room = roomCommandPort.getByIdOrThrow(command.roomId());
        room.validateRoomInProgress();

        // 2. 유저가 해당 방에서 오늘 이미 작성한 오늘의 한마디 개수 조회
        int alreadyWrittenCountToday = attendanceCheckQueryPort.countAttendanceChecksOnTodayByUser(command.creatorId(), command.roomId());

        // 3. 출석체크 도메인 생성 및 저장
        AttendanceCheck attendanceCheck = AttendanceCheck.withoutId(command.roomId(), command.creatorId(), command.content(), alreadyWrittenCountToday);
        AttendanceCheck SavedattendanceCheck = attendanceCheckCommandPort.getByIdOrThrow(attendanceCheckCommandPort.save(attendanceCheck));

        return AttendanceCheckCreateResult.builder()
                .roomId(command.roomId())
                .attendanceCheckId(SavedattendanceCheck.getId())
                .creatorId(command.creatorId())
                .creatorNickname(user.getNickname())
                .creatorProfileImageUrl(user.getAlias().getImageUrl())
                .todayComment(command.content())
                .postDate(DateUtil.formatBeforeTime(SavedattendanceCheck.getCreatedAt()))
                .date(SavedattendanceCheck.getCreatedAt().toLocalDate())
                .todayWriteCountOfUser(alreadyWrittenCountToday + 1)
                .isWriter(true)
                .build();
    }
}
