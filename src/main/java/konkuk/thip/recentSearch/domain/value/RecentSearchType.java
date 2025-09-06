package konkuk.thip.recentSearch.domain.value;


import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_SEARCH_TYPE;

@Getter
public enum RecentSearchType {

    USER_SEARCH("USER"),
    BOOK_SEARCH("BOOK"),
    ROOM_SEARCH("ROOM"),
    ;

    private final String searchType;

    RecentSearchType(String searchType) {
        this.searchType = searchType;
    }

    public static RecentSearchType from(String searchType) {
        for (RecentSearchType type : RecentSearchType.values()) {
            if (type.getSearchType().equals(searchType)) {
                return type;
            }
        }
        throw new InvalidStateException(INVALID_SEARCH_TYPE);
    }
}