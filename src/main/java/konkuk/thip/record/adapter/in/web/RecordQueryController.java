package konkuk.thip.record.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.application.port.in.dto.RecordSearchQuery;
import konkuk.thip.record.application.port.in.dto.RecordSearchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecordQueryController {

    private final RecordSearchUseCase recordSearchUseCase;

    /**
     * 방의 게시글(기록, 투표) 목록 조회
     * @param roomId
     * @param type : group , mine
     * @param sort : 그룹 기록 -> 최신순 / 내 기록 -> 페이지 높은 순 default 정렬
     * @param pageStart
     * @param pageEnd
     * @param isOverview : 총평보기 필터 여부
     * @param isPageFilter : 페이지 필터 여부
     * @param userId
     * @return
     */
    @GetMapping("/rooms/{roomId}/posts")
    public BaseResponse<RecordSearchResponse> viewRecordList(
            @PathVariable final Long roomId,
            @RequestParam(required = false, defaultValue = "group") final String type,
            @RequestParam(required = false) final String sort,
            @RequestParam(required = false) final Integer pageStart,
            @RequestParam(required = false) final Integer pageEnd,
            @RequestParam(required = false, defaultValue = "false") final Boolean isOverview,
            @RequestParam(required = false, defaultValue = "false") final Boolean isPageFilter,
            @RequestParam(required = false) final String cursor,
            @UserId final Long userId
    ) {
        return BaseResponse.ok(recordSearchUseCase.search(
                RecordSearchQuery.builder()
                        .roomId(roomId)
                        .type(type)
                        .sort(sort)
                        .pageStart(pageStart)
                        .pageEnd(pageEnd)
                        .isOverview(isOverview)
                        .isPageFilter(isPageFilter)
                        .nextCursor(cursor)
                        .userId(userId)
                        .build()
        ));
    }

}
