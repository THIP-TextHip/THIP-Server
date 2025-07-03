package konkuk.thip.recentSearch.adapter.out.jpa;


import konkuk.thip.common.exception.BusinessException;
import lombok.Getter;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_SEARCH_TYPE;

@Getter
public enum SearchType {

    USER_SEARCH("사용자 검색"),
    BOOK_SEARCH("책_검색");

    private final String searchType;

    SearchType(String searchType) {
        this.searchType = searchType;
    }

    public static SearchType from(String searchType) {
        for (SearchType type : SearchType.values()) {
            if (type.getSearchType().equals(searchType)) {
                return type;
            }
        }
        throw new BusinessException(INVALID_SEARCH_TYPE);
    }
}