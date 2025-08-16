package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.application.port.in.RoomGetHomeJoinedListUseCase;
import konkuk.thip.room.application.port.in.dto.RoomGetHomeJoinedListQuery;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RoomGetHomeJoinedListService implements RoomGetHomeJoinedListUseCase {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final RoomQueryPort roomQueryPort;
    private final UserCommandPort userCommandPort;

    @Override
    @Transactional(readOnly = true)
    public RoomGetHomeJoinedListResponse getHomeJoinedRoomList(RoomGetHomeJoinedListQuery query) {

        // 1. page 값 검증
        validatePage(query.page());

        // 2. 유저 닉네임 조회
        String nickname = userCommandPort.findById(query.userId()).getNickname();

        // 3. Pageable 생성
        int pageIndex = query.page() > 0 ? query.page() - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE);

        // 4. 모임 홈에서 참여중인 모임 방 검색
        Page<RoomGetHomeJoinedListResponse.JoinedRoomInfo> result = roomQueryPort.searchHomeJoinedRooms(query.userId(), LocalDate.now(), pageable);

        // 5. response 구성
        return RoomGetHomeJoinedListResponse.builder()
                .roomList(result.getContent())
                .nickname(nickname)
                .page(query.page())
                .size(result.getNumberOfElements())
                .last(result.isLast())
                .first(result.isFirst())
                .build();
    }

    private void validatePage(int page) {
        if(page< 1) {
            throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("page은 1 이상의 값이어야 합니다."));
        }
    }

}
