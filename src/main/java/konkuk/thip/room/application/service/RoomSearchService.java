package konkuk.thip.room.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.recentSearch.domain.value.RecentSearchType;
import konkuk.thip.recentSearch.application.service.manager.RecentSearchCreateManager;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.mapper.RoomQueryMapper;
import konkuk.thip.room.application.port.in.dto.RoomSearchSortParam;
import konkuk.thip.room.application.port.in.RoomSearchUseCase;
import konkuk.thip.room.application.port.in.dto.RoomSearchQuery;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.value.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomSearchService implements RoomSearchUseCase {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final RoomQueryPort roomQueryPort;
    private final RecentSearchCreateManager recentSearchCreateManager;
    private final RoomQueryMapper roomQueryMapper;

    @Override
    @Transactional // <- 최근 검색 저장으로 인한 트랜잭션
    public RoomSearchResponse searchRecruitingRooms(RoomSearchQuery query) {
        // 1. validation
        RoomSearchSortParam sortParam = RoomSearchSortParam.from(query.sortStr());
        Category category = validateCategory(query.categoryStr());

        // 2. Cursor 생성
        Cursor cursor = Cursor.from(query.cursorStr(), DEFAULT_PAGE_SIZE);

        // 3. 방 검색
        CursorBasedList<RoomQueryDto> result = executeRecruitingRoomSearch(query, category, sortParam, cursor);

        // 4. 검색 완료일 경우, 최근 검색어 저장
        recentSearchCreateManager.saveRecentSearchByUser(query.userId(), query.keyword(), RecentSearchType.ROOM_SEARCH, query.isFinalized());

        // 5. response 구성
        return new RoomSearchResponse(
                roomQueryMapper.toRoomSearchResponse(result.contents()),
                result.nextCursor(),
                result.isLast()
        );
    }

    private CursorBasedList<RoomQueryDto> executeRecruitingRoomSearch(RoomSearchQuery query, Category category, RoomSearchSortParam sortParam, Cursor cursor) {
        CursorBasedList<RoomQueryDto> result = null;
        if (category == null) {
            switch (sortParam) {
                case DEADLINE:
                    return roomQueryPort.searchRecruitingRoomsByDeadline(query.keyword(), cursor);
                case MEMBER_COUNT:
                    return roomQueryPort.searchRecruitingRoomsByMemberCount(query.keyword(), cursor);
            }
        } else {
            switch (sortParam) {
                case DEADLINE:
                    return roomQueryPort.searchRecruitingRoomsWithCategoryByDeadline(query.keyword(), category, cursor);
                case MEMBER_COUNT:
                    return roomQueryPort.searchRecruitingRoomsWithCategoryByMemberCount(query.keyword(), category, cursor);
            }
        }
        return result;
    }

    private Category validateCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.isEmpty()) {
            return null;
        }

        return Category.from(categoryStr);
    }
}
