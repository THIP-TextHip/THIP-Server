package konkuk.thip.book.adapter.out.api.aladin;

public enum AladinApiParam {

    ITEM_ID_TYPE("ISBN"),
    OUTPUT("js"),
    API_VERSION("20131101"),
    SUB_INFO_PARSING_KEY("subInfo"),
    PAGE_COUNT_PARSING_KEY("itemPage");

    private final String value;

    AladinApiParam(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
