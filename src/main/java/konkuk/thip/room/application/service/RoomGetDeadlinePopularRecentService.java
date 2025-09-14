package konkuk.thip.room.application.service;

import konkuk.thip.room.adapter.in.web.response.RoomGetDeadlinePopularRecentResponse;
import konkuk.thip.room.application.mapper.RoomQueryMapper;
import konkuk.thip.room.application.port.in.RoomGetDeadlinePopularRecentUseCase;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.domain.value.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomGetDeadlinePopularRecentService implements RoomGetDeadlinePopularRecentUseCase {

    private final RoomQueryPort roomQueryPort;
    private final RoomQueryMapper roomQueryMapper;

    private static final int DEFAULT_LIMIT = 4;

    @Override
    @Transactional(readOnly = true)
    public RoomGetDeadlinePopularRecentResponse getDeadlineAndPopularAndRecentRoomList(String categoryStr) {
        Category category = Category.from(categoryStr);

        var deadlineRoomList = roomQueryMapper.toDeadlinePopularRecentRoomDtoList(
                roomQueryPort.findRoomsByCategoryOrderByDeadline(category, DEFAULT_LIMIT));
        var popularRoomList = roomQueryMapper.toDeadlinePopularRecentRoomDtoList(
                roomQueryPort.findRoomsByCategoryOrderByPopular(category, DEFAULT_LIMIT));
        var recentRoomList = roomQueryMapper.toDeadlinePopularRecentRoomDtoList(
                roomQueryPort.findRoomsByCategoryOrderByRecent(category, DEFAULT_LIMIT));

        return RoomGetDeadlinePopularRecentResponse.of(deadlineRoomList, popularRoomList,recentRoomList);
    }

}
