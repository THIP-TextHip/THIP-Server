package konkuk.thip.recentSearch.adapter.in.web.response;

import java.util.List;

public record RecentSearchGetResponse(
        List<String> recentSearchList
) {
    public static RecentSearchGetResponse of(List<String> recentSearchList) {
        return new RecentSearchGetResponse(recentSearchList);
    }
}
