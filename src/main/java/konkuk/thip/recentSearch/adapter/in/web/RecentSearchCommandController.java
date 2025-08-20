package konkuk.thip.recentSearch.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.recentSearch.application.port.in.RecentSearchDeleteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.RECENT_SEARCH_DELETE;

@Tag(name = "Recent Search Command API", description = "최근 검색어 상태 변경 관련 API")
@RestController
@RequiredArgsConstructor
public class RecentSearchCommandController {

    private final RecentSearchDeleteUseCase recentSearchDeleteUseCase;

    @Operation(summary = "최근 검색어 삭제", description = "최근 검색어를 삭제합니다.")
    @ExceptionDescription(RECENT_SEARCH_DELETE)
    @DeleteMapping("/recent-searches/{recentSearchId}")
    public BaseResponse<Void> deleteRecentSearch(
            @PathVariable(value = "recentSearchId") final Long recentSearchId,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(recentSearchDeleteUseCase.deleteRecentSearch(recentSearchId, userId));
    }

}
