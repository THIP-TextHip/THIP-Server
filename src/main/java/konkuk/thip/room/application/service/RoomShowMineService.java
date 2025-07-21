package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.application.port.in.RoomShowMineUseCase;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.CursorSliceOfMyRoomView;
import konkuk.thip.room.domain.MyRoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_MY_ROOM_CURSOR;

@Service
@RequiredArgsConstructor
public class RoomShowMineService implements RoomShowMineUseCase {

    private final static int PAGE_SIZE = 10;

    private final RoomQueryPort roomQueryPort;

    @Override
    @Transactional(readOnly = true)
    public RoomShowMineResponse getMyRooms(Long userId, String type, LocalDate cursorDate, Long cursorId) {
        // 1. cursor xor 연산 검증
        if (cursorDate == null ^ cursorId == null) {
            throw new BusinessException(INVALID_MY_ROOM_CURSOR, new IllegalArgumentException("cursorDate, cursorId는 하나만 null 일 수 없습니다."));
        }

        // 2. type 검증 및 커서 기반 조회
        CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> slice = switch (MyRoomType.from(type)) {
                case RECRUITING -> roomQueryPort
                        .findRecruitingRoomsUserParticipated(userId, cursorDate, cursorId, PAGE_SIZE);
                case PLAYING    -> roomQueryPort
                        .findPlayingRoomsUserParticipated(userId, cursorDate, cursorId, PAGE_SIZE);
                case PLAYING_AND_RECRUITING -> roomQueryPort
                    .findPlayingAndRecruitingRoomsUserParticipated(userId, cursorDate, cursorId, PAGE_SIZE);
                case EXPIRED    -> roomQueryPort
                        .findExpiredRoomsUserParticipated(userId, cursorDate, cursorId, PAGE_SIZE);
        };

        // 3. return
        return new RoomShowMineResponse(
                slice.getContent(),
                slice.getContent().size(),
                slice.isLast(),
                slice.getNextCursorDate(),
                slice.getNextCursorId()
        );
    }
}
