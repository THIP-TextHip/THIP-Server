package konkuk.thip.room.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.application.mapper.RoomParticipantQueryMapper;
import konkuk.thip.room.application.port.in.RoomGetHomeJoinedListUseCase;
import konkuk.thip.room.application.port.in.dto.RoomGetHomeJoinedListQuery;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomParticipantQueryDto;
import konkuk.thip.user.application.port.out.UserCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomGetHomeJoinedListService implements RoomGetHomeJoinedListUseCase {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final RoomQueryPort roomQueryPort;
    private final UserCommandPort userCommandPort;
    private final RoomParticipantQueryMapper roomParticipantQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public RoomGetHomeJoinedListResponse getHomeJoinedRoomList(RoomGetHomeJoinedListQuery query) {

        // 1. 유저 닉네임 조회
        String nickname = userCommandPort.findById(query.userId()).getNickname();

        // 2. Cursor 생성
        Cursor cursor = Cursor.from(query.cursorStr(), DEFAULT_PAGE_SIZE);

        // 3. 모임 홈에서 참여중인 모임 방 검색
        CursorBasedList<RoomParticipantQueryDto> result = roomQueryPort.searchHomeJoinedRooms(query.userId(), cursor);

        return RoomGetHomeJoinedListResponse.of(
                roomParticipantQueryMapper.toHomeJoinedRoomResponse(result.contents()),
                nickname,result.nextCursor(),result.isLast());
    }

}
