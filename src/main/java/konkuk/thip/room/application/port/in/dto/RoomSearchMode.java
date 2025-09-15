package konkuk.thip.room.application.port.in.dto;

import konkuk.thip.room.domain.value.Category;

public enum RoomSearchMode{
    GLOBAL_BY_KEYWORD_OR_ALL, // 카테고리 없음: 전체 또는 키워드 기반
    CATEGORY_ALL,             // 카테고리 있음 + 전체조회 (키워드 비어있고 isAllCategory=true)
    CATEGORY_BY_KEYWORD;       // 카테고리 있음 + 키워드 기반(또는 전체가 아님)

    public static RoomSearchMode determineSearchMode(Category category, boolean isAllCategory, String keyword) {
        final boolean isKeywordEmpty = isEmpty(keyword);

        if (category == null) {
            // 카테고리 없음 → 전체 또는 키워드 기반
            // 유효성 검증에서 이미 조합을 보장하므로 그대로 처리
            return RoomSearchMode.GLOBAL_BY_KEYWORD_OR_ALL;
        }

        // 카테고리 있음
        if (isAllCategory && isKeywordEmpty) {
            return RoomSearchMode.CATEGORY_ALL;
        }
        return RoomSearchMode.CATEGORY_BY_KEYWORD;
    }

    public static String resolveEffectiveKeyword(RoomSearchMode mode, String keyword) {
        final boolean isKeywordEmpty = isEmpty(keyword);
        return switch (mode) {
            case GLOBAL_BY_KEYWORD_OR_ALL ->
                // 전체(isAllCategory=true)이며 키워드 비어있을 수 있으므로 빈 문자열 허용
                    isKeywordEmpty ? "" : keyword.trim();
            case CATEGORY_ALL ->
                // 카테고리 전체 조회: 키워드 강제 빈 문자열
                    "";
            case CATEGORY_BY_KEYWORD ->
                // 키워드가 비어있을 수도 있지만, 의미적으로 "전체가 아님"이므로
                // 저장 계층에서 빈 문자열이면 전체와 동일해질 수 있음 → 그대로 전달(정책 유지)
                    isKeywordEmpty ? "" : keyword.trim();
        };
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}


