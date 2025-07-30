package konkuk.thip.recentSearch.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.recentSearch.adapter.in.web.response.RecentSearchGetResponse;
import konkuk.thip.recentSearch.application.port.in.RecentSearchGetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recent Search Query API", description = "최근 검색어 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class RecentSearchQueryController {

    private final RecentSearchGetUseCase recentSearchGetUseCase;

    @Operation(summary = "최근 검색어 조회", description = "사용자의 최근 검색어를 조회합니다. 최신순으로 최대 5개까지 조회됩니다.")
    @GetMapping("/recent-searches")
    public BaseResponse<RecentSearchGetResponse> showRecentSearches(
            @Parameter(description = "최근 검색어 유형 (사용자 검색 : USER / 방 검색 : ROOM / 책 검색 : BOOK)", example = "USER")
            @RequestParam(value = "type") String type,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(recentSearchGetUseCase.getRecentSearches(type, userId));
    }

}
