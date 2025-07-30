package konkuk.thip.recentSearch.adapter.in.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.recentSearch.adapter.in.web.response.RecentSearchGetResponse;
import konkuk.thip.recentSearch.application.port.in.RecentSearchGetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recent Search Query API", description = "최근 검색어 조회 API")
@RestController
@RequiredArgsConstructor
public class RecentSearchQueryController {

    private final RecentSearchGetUseCase recentSearchGetUseCase;

    @GetMapping("/recent-searches")
    public BaseResponse<RecentSearchGetResponse> showRecentSearches(
            @RequestParam(value = "type") String type,
            @UserId final Long userId
    ) {
        return BaseResponse.ok(RecentSearchGetResponse.of(recentSearchGetUseCase.getRecentSearches(type, userId)));
    }

}
