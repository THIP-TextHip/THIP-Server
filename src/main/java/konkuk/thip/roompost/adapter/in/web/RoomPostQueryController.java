package konkuk.thip.roompost.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.roompost.adapter.in.web.response.RecordPinResponse;
import konkuk.thip.roompost.adapter.in.web.response.RoomPostSearchResponse;
import konkuk.thip.roompost.application.port.in.RecordPinUseCase;
import konkuk.thip.roompost.application.port.in.RoomPostSearchUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordPinQuery;
import konkuk.thip.roompost.application.port.in.dto.RoomPostSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.RECORD_PIN;
import static konkuk.thip.common.swagger.SwaggerResponseDescription.RECORD_SEARCH;

@Tag(name = "RoomPost Query API", description = "방 게시글 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class RoomPostQueryController {

    private final RoomPostSearchUseCase roomPostSearchUseCase;
    private final RecordPinUseCase recordPinUseCase;

    @Operation(
            summary = "방의 게시글(기록, 투표) 목록 조회",
            description = "방의 게시글(기록, 투표) 목록을 조회합니다. type에 따라 그룹 기록 또는 내 기록을 조회할 수 있습니다."
    )
    @ExceptionDescription(RECORD_SEARCH)
    @GetMapping("/rooms/{roomId}/posts")
    public BaseResponse<RoomPostSearchResponse> viewRecordList(
            @Parameter(description = "게시글을 조회할 방 ID", example = "1") @PathVariable final Long roomId,
            @Parameter(description = "게시글 조회 타입 (group: 그룹 기록, mine: 내 기록)", example = "group")
            @RequestParam(required = false, defaultValue = "group") final String type,
            @Parameter(description = "게시글 정렬 기준 (최신순: latest, 인기 순: like, 댓글 많은 순: comment) \n그룹 기록에서만 사용됩니다. 내 기록에서는 페이지 높은 순 고정 정렬", example = "latest")
            @RequestParam(required = false) final String sort,
            @Parameter(description = "게시글 페이지 필터링 시작 범위 (default: 0)", example = "10")
            @RequestParam(required = false) final Integer pageStart,
            @Parameter(description = "게시글 페이지 필터링 종료 범위 (default: 책의 마지막 페이지)", example = "100")
            @RequestParam(required = false) final Integer pageEnd,
            @Parameter(description = "총평 보기 필터 여부 (default: false)", example = "true")
            @RequestParam(required = false, defaultValue = "false") final Boolean isOverview,
            @Parameter(description = "페이지 필터 여부 (default: false)", example = "true")
            @RequestParam(required = false, defaultValue = "false") final Boolean isPageFilter,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(required = false) final String cursor,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(roomPostSearchUseCase.search(
                RoomPostSearchQuery.builder()
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

    @Operation(
            summary = "기록을 피드에 핀하기 및 피드 핀을 위한 책 정보 조회",
            description = "사용자가 기록을 피드에 핀할 수 있는지 검증 및, 피드 핀할시 필요한 책 정보를 조회합니다"
    )
    @ExceptionDescription(RECORD_PIN)
    @GetMapping("/rooms/{roomId}/records/{recordId}/pin")
    public BaseResponse<RecordPinResponse> pinRecord(
            @Parameter(description = "핀하려는 기록이 작성된 모임 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(description = "핀하려는 기록 ID", example = "1") @PathVariable("recordId") final Long recordId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(recordPinUseCase.pinRecord(new RecordPinQuery(roomId, recordId, userId)));
    }

}
