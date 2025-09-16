package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.recentSearch.domain.value.RecentSearchType;
import konkuk.thip.recentSearch.application.service.manager.RecentSearchCreateManager;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.mapper.RoomQueryMapper;
import konkuk.thip.room.application.port.in.dto.RoomSearchMode;
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
    @Transactional
    public RoomSearchResponse searchRecruitingRooms(RoomSearchQuery query) {
        // 1) 파라미터 파싱/검증
        RoomSearchSortParam sortParam = RoomSearchSortParam.from(query.sortStr());
        validateSearchParams(query.keyword(), query.isAllCategory(), query.categoryStr());
        Category category = validateCategory(query.categoryStr());

        // 2) 검색 모드/키워드 결정
        RoomSearchMode mode = RoomSearchMode.determineSearchMode(category, query.isAllCategory(), query.keyword());
        String effectiveKeyword = RoomSearchMode.resolveEffectiveKeyword(mode, query.keyword());

        // 3) 커서 생성
        Cursor cursor = Cursor.from(query.cursorStr(), DEFAULT_PAGE_SIZE);

        // 4) 실행 (정렬 기준별 단일 switch)
        CursorBasedList<RoomQueryDto> result = executeSearchMode(mode, sortParam, effectiveKeyword, category, cursor);

        // 5) 최근 검색어 저장
        recentSearchCreateManager.saveRecentSearchByUser(
                query.userId(), query.keyword(), RecentSearchType.ROOM_SEARCH, query.isFinalized()
        );

        // 6) 응답 매핑
        return new RoomSearchResponse(
                roomQueryMapper.toRoomSearchResponse(result.contents()),
                result.nextCursor(),
                result.isLast()
        );
    }

    private CursorBasedList<RoomQueryDto> executeSearchMode(
            RoomSearchMode mode,
            RoomSearchSortParam sort,
            String keyword,
            Category category,
            Cursor cursor
    ) {
        return switch (sort) {
            case DEADLINE -> executeByDeadline(mode, keyword, category, cursor);
            case MEMBER_COUNT -> executeByMemberCount(mode, keyword, category, cursor);
            default -> throw new BusinessException(
                    API_INVALID_PARAM,
                    new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + sort)
            );
        };
    }

    private CursorBasedList<RoomQueryDto> executeByDeadline(
            RoomSearchMode mode, String keyword, Category category, Cursor cursor
    ) {
        return switch (mode) {
            case GLOBAL_BY_KEYWORD_OR_ALL -> roomQueryPort.searchRecruitingRoomsByDeadline(keyword, cursor);
            case CATEGORY_ALL, CATEGORY_BY_KEYWORD -> roomQueryPort.searchRecruitingRoomsWithCategoryByDeadline(keyword, category, cursor);
        };
    }

    private CursorBasedList<RoomQueryDto> executeByMemberCount(
            RoomSearchMode mode, String keyword, Category category, Cursor cursor
    ) {
        return switch (mode) {
            case GLOBAL_BY_KEYWORD_OR_ALL -> roomQueryPort.searchRecruitingRoomsByMemberCount(keyword, cursor);
            case CATEGORY_ALL, CATEGORY_BY_KEYWORD -> roomQueryPort.searchRecruitingRoomsWithCategoryByMemberCount(keyword, category, cursor);
        };
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
