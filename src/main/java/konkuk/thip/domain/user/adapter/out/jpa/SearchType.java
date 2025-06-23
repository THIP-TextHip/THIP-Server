package konkuk.thip.domain.user.adapter.out.jpa;


import lombok.Getter;

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
        //컨트롤러 어드바이스 추가하고 예외처리
        //throw new GlobalException(INVALID_SEARCH_TYPE);
        return null;
    }
}