package konkuk.thip.recentSearch.adapter.in.web.response;

import java.util.List;

public record RecentSearchGetResponse(
        List<RecentSearchDto> recentSearchList
) {

    public record RecentSearchDto(
            Long recentSearchId,
            String searchTerm
    ) {
    }
    public static RecentSearchGetResponse of(List<RecentSearchDto> recentSearchList) {
        return new RecentSearchGetResponse(recentSearchList);
    }
}
