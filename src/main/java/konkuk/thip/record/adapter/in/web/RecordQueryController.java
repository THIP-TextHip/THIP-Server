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

    @GetMapping("/rooms/{roomId}/posts")
    public BaseResponse<RecordSearchResponse> viewRecordList(
            @PathVariable final Long roomId,
            @RequestParam(required = false) final String type,
            @RequestParam(required = false) final String sort,
            @RequestParam(required = false) final Integer pageStart,
            @RequestParam(required = false) final Integer pageEnd,
            @RequestParam final Boolean isOverview,
            @RequestParam final Integer pageNum,
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
                        .pageNum(pageNum)
                        .userId(userId)
                        .build()
        ));
    }

}
