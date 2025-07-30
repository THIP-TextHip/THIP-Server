package konkuk.thip.recentSearch.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_SEARCH_TYPE;

@Getter
public enum RecentSearchType {

    USER_SEARCH("USER","사용자 검색"),
    BOOK_SEARCH("BOOK","책 검색"),
    ROOM_SEARCH("ROOM","방 검색");

    private final String param;
    private final String type;

    RecentSearchType(String param, String type) {
        this.param = param;
        this.type = type;
    }

    public static RecentSearchType from(String param) {
        for (RecentSearchType recentSearchType : RecentSearchType.values()) {
            if (recentSearchType.getParam().equals(param)) {
                return recentSearchType;
            }
        }
        throw new InvalidStateException(INVALID_SEARCH_TYPE);
    }
}
