package konkuk.thip.room.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.application.mapper.RoomQueryMapper;
import konkuk.thip.room.application.port.in.RoomShowMineUseCase;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.application.port.in.dto.MyRoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoomShowMineService implements RoomShowMineUseCase {

    private final static int PAGE_SIZE = 10;

    private final RoomQueryPort roomQueryPort;
    private final RoomQueryMapper roomQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public RoomShowMineResponse getMyRooms(Long userId, String type, String cursor) {
        // 1. cursor 생성
        Cursor nextCursor = Cursor.from(cursor, PAGE_SIZE);

        // 2. type 검증 및 커서 기반 조회
        MyRoomType myRoomType = MyRoomType.from(type);
        CursorBasedList<RoomQueryDto> result = switch (myRoomType) {
                case RECRUITING -> roomQueryPort
                        .findRecruitingRoomsUserParticipated(userId, nextCursor);
                case PLAYING    -> roomQueryPort
                        .findPlayingRoomsUserParticipated(userId, nextCursor);
                case PLAYING_AND_RECRUITING -> roomQueryPort
                    .findPlayingAndRecruitingRoomsUserParticipated(userId, nextCursor);
                case EXPIRED    -> roomQueryPort
                        .findExpiredRoomsUserParticipated(userId, nextCursor);
        };

        // 3. dto -> response로 매핑 (EXPIRED 타입인 경우 endDate를 null로 처리)
        var myRooms = result.contents().stream()
                .map(dto -> {
                    if (myRoomType != MyRoomType.PLAYING_AND_RECRUITING) {
                        // PLAYING, RECRUITING, EXPIRED 경우 타입 그대로 전달
                        return roomQueryMapper.toShowMyRoomResponse(dto, myRoomType);
                    }

                    // PLAYING_AND_RECRUITING: startDate 기준으로 진행중/모집중 구분
                    LocalDate now = LocalDate.now();
                    if (dto.startDate() != null && dto.startDate().isBefore(now)) {
                        return roomQueryMapper.toShowMyRoomResponse(dto, MyRoomType.PLAYING);   // 진행중인 방
                    } else {
                        return roomQueryMapper.toShowMyRoomResponse(dto, MyRoomType.RECRUITING);    // 모집중인 방
                    }
                })
                .toList();

        return new RoomShowMineResponse(
                myRooms,
                result.nextCursor(),
                !result.hasNext()
        );
    }
}
