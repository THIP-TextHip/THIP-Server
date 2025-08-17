package konkuk.thip.roompost.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.adapter.in.web.response.AttendanceCheckShowResponse;
import konkuk.thip.roompost.application.mapper.AttendanceCheckQueryMapper;
import konkuk.thip.roompost.application.port.in.AttendanceCheckShowUseCase;
import konkuk.thip.roompost.application.port.out.AttendanceCheckQueryPort;
import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceCheckShowService implements AttendanceCheckShowUseCase {

    private static final int PAGE_SIZE = 10;
    private final RoomParticipantValidator roomParticipantValidator;
    private final AttendanceCheckQueryPort attendanceCheckQueryPort;
    private final AttendanceCheckQueryMapper attendanceCheckQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public AttendanceCheckShowResponse showDailyGreeting(Long userId, Long roomId, String cursorStr) {
        // 1. 유저가 방 멤버가 맞는지 검사
        roomParticipantValidator.validateUserIsRoomMember(roomId, userId);

        // 2. Cursor 생성
        Cursor cursor = Cursor.from(cursorStr, PAGE_SIZE);

        // 3. 오늘의 한마디 조회
        CursorBasedList<AttendanceCheckQueryDto> dtos = attendanceCheckQueryPort.findAttendanceChecksByCreatedAtDesc(roomId, cursor);

        // 4. response 로 매핑 후 반환
        return new AttendanceCheckShowResponse(
                attendanceCheckQueryMapper.toAttendanceCheckShowResponse(dtos.contents(), userId),
                dtos.nextCursor(),
                dtos.isLast()
        );
    }
}
