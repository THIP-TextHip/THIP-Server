package konkuk.thip.room.application.service;

import konkuk.thip.room.adapter.in.web.response.RoomGetDeadlinePopularResponse;
import konkuk.thip.room.application.mapper.RoomQueryMapper;
import konkuk.thip.room.application.port.in.RoomGetDeadlinePopularUseCase;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.domain.value.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomGetDeadlinePopularService implements RoomGetDeadlinePopularUseCase {

    private final RoomQueryPort roomQueryPort;
    private final RoomQueryMapper roomQueryMapper;

    private static final int DEFAULT_LIMIT = 4;

    @Override
    @Transactional(readOnly = true)
    public RoomGetDeadlinePopularResponse getDeadlineAndPopularRoomList(String categoryStr, Long userId) {
        Category category = Category.from(categoryStr);

        var deadlineRoomList = roomQueryMapper.toDeadlinePopularRoomDtoList(
                roomQueryPort.findRoomsByCategoryOrderByDeadline(category, DEFAULT_LIMIT, userId));
        var popularRoomList = roomQueryMapper.toDeadlinePopularRoomDtoList(
                roomQueryPort.findRoomsByCategoryOrderByPopular(category, DEFAULT_LIMIT, userId));

        return RoomGetDeadlinePopularResponse.of(deadlineRoomList, popularRoomList);
    }
}
