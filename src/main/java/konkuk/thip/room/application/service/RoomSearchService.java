package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchType;
import konkuk.thip.recentSearch.application.service.manager.RecentSearchCreateManager;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.adapter.out.persistence.RoomSearchSortParam;
import konkuk.thip.room.application.port.in.RoomSearchUseCase;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_ROOM_SEARCH_SORT;

@Service
@RequiredArgsConstructor
public class RoomSearchService implements RoomSearchUseCase {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final RoomQueryPort roomQueryPort;

    private final RecentSearchCreateManager recentSearchCreateManager;

    @Override
    @Transactional // <- 최근 검색 저장으로 인한 트랜잭션
    public RoomSearchResponse searchRoom(String keyword, String category, String sort, int page, boolean isFinalized, Long userId) {
        // 1. validation
        String sortVal = validateSort(sort);
        String categoryVal = validateCategory(category);

        // 2. Pageable 생성
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE, buildSort(sortVal));

        // 3. 방 검색
        Page<RoomSearchResponse.RoomSearchResult> result = roomQueryPort.searchRoom(keyword, categoryVal, pageable);

        // TODO 검색 완료일 경우, 최근 검색어로 저장되도록
        recentSearchCreateManager.saveRecentSearchByUser(userId, keyword, RecentSearchType.ROOM_SEARCH, isFinalized);

        // 4. response 구성
        return new RoomSearchResponse(
                result.getContent(),
                page,
                result.getNumberOfElements(),
                result.isLast(),
                result.isFirst());
    }

    private String validateSort(String sort) {
        try {
            return RoomSearchSortParam.from(sort).getValue();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(INVALID_ROOM_SEARCH_SORT, ex);
        }
    }

    private String validateCategory(String category) {
        if (category == null || category.isEmpty()) {
            return "";
        }

        return Category.from(category).getValue();
    }

    /**
     * 정렬 키에 따른 Sort 객체 생성
     */
    private Sort buildSort(String sortVal) {
        if (sortVal.equals(RoomSearchSortParam.MEMBER_COUNT.getValue())) {
            // 인기순: 참여자 수 내림차순
            return Sort.by(Sort.Direction.DESC, RoomSearchSortParam.MEMBER_COUNT.getValue());
        }
        if (sortVal.equals(RoomSearchSortParam.RECOMMEND.getValue())) {
            // TODO: 추후 추천 로직 구현 시 반영
            return Sort.unsorted();
        }
        // default: 마감 임박순(deadLine) = 시작일 빠른 순서대로 오름차순
        return Sort.by(Sort.Direction.ASC, RoomSearchSortParam.DEADLINE.getValue());
    }
}
