package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.BusinessException;
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

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;

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
        validateSearchParams(query.keyword(),query.isAllCategory(),query.categoryStr());
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
        boolean isAllCategory = query.isAllCategory();
        String keyword = query.keyword();
        boolean isKeywordEmpty = (keyword == null || keyword.trim().isEmpty());

        // 빈 키워드이면서 isAllCategory가 true인 경우는 전체 조회를 위해 빈 문자열을 사용하고, 그렇지 않으면 그대로 keyword를 사용
        String effectiveKeyword = isKeywordEmpty && isAllCategory ? "" : keyword;

        if (category == null) {
            // 전체 카테고리 중에서
            // 1) 전체검색(isAllCategory=true)이거나
            // 2) 키워드가 비어있지 않은 경우
            // 해당 조건 모두 포함해서 키워드 기반 검색 또는 전체 검색 수행
            if (isAllCategory || !isKeywordEmpty) {
                switch (sortParam) {
                    case DEADLINE:
                        return roomQueryPort.searchRecruitingRoomsByDeadline(effectiveKeyword, cursor);
                    case MEMBER_COUNT:
                        return roomQueryPort.searchRecruitingRoomsByMemberCount(effectiveKeyword, cursor);
                }
            }
        } else {
            if (isAllCategory && isKeywordEmpty) {
                // isAllCategory가 true이고, 키워드가 비어있으면
                // 특정 카테고리 내에서 '전체 조회'를 의미함 즉, 키워드 없이 카테고리 필터만 적용해서 전체 방 조회
                switch (sortParam) {
                    case DEADLINE:
                        return roomQueryPort.searchRecruitingRoomsWithCategoryByDeadline("", category, cursor);
                    case MEMBER_COUNT:
                        return roomQueryPort.searchRecruitingRoomsWithCategoryByMemberCount("", category, cursor);
                }
            } else if (!isAllCategory) {
                // isAllCategory가 false인 경우 (전체검색 아님)
                // category가 존재하고 키워드는 있거나 빈 문자열이어도 키워드 기반 조회 수행
                switch (sortParam) {
                    case DEADLINE:
                        return roomQueryPort.searchRecruitingRoomsWithCategoryByDeadline(effectiveKeyword, category, cursor);
                    case MEMBER_COUNT:
                        return roomQueryPort.searchRecruitingRoomsWithCategoryByMemberCount(effectiveKeyword, category, cursor);
                }
            }
        }
        return null;
    }

    private Category validateCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.isEmpty()) {
            return null;
        }

        return Category.from(categoryStr);
    }

    private void validateSearchParams(String keyword, boolean isAllCategory, String categoryStr) {
        boolean isKeywordEmpty = (keyword == null || keyword.trim().isEmpty());
        boolean isCategoryEmpty = (categoryStr == null || categoryStr.trim().isEmpty());

        // 키워드와 카테고리 둘 다 없을 때 isAllCategory가 true여야 함
        if (isKeywordEmpty && isCategoryEmpty && !isAllCategory) {
            throw new BusinessException(API_INVALID_PARAM,
                    new IllegalArgumentException("검색어와 카테고리가 없을 경우, 전체 검색을 명시하는 isAllCategory=true 옵션이 필요합니다."));
        }

        // 기존 예외 : 키워드 있는데 isAllCategory=true 이면서 특정 카테고리 존재 불가
        if (isAllCategory && !isKeywordEmpty && !isCategoryEmpty) {
            throw new BusinessException(API_INVALID_PARAM,
                    new IllegalArgumentException("키워드가 있을 때 특정 카테고리 검색과 전체검색(isAllCategory=true)을 동시에 사용할 수 없습니다."));
        }
    }

}
