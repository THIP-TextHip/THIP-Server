package konkuk.thip.roompost.application.service.validator;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;

@HelperService
public class RoomPostAccessValidator {

    private static final String BLURRED_STRING = "태정태세문단세예성연중인명선광인효현숙경영정순헌철고순";

    public void validateGroupRoomPostFilters(Integer pageStart, Integer pageEnd, Boolean isPageFilter, Boolean isOverview, int bookPageSize, double currentPercentage) {
        if(!isPageFilter && !isOverview) { // 어떤 필터도 적용되지 않는 경우
            if (pageStart != null || pageEnd != null) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("어떤 필터도 적용되지 않는 경우 pageStart와 pageEnd는 null이어야 합니다."));
            }
        }
        if(!isPageFilter && isOverview) { // 총평보기 필터만 적용된 경우
            if (pageStart != null || pageEnd != null) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("총평보기 필터만 적용된 경우 pageStart와 pageEnd는 null이어야 합니다."));
            }
            if (currentPercentage < 80) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("총평보기 필터가 적용된 경우 현재 독서 진행률은 80% 이상이어야 합니다."));
            }
        }
        if(isPageFilter && !isOverview) { // 페이지 필터만 적용된 경우는 pageStart와 pageEnd가 null이여도 됨
            if(pageStart != null && (pageStart < 0 || pageStart > bookPageSize)) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart는 책의 페이지 범위 내에 있어야 합니다."));
            }
            if(pageEnd != null && (pageEnd < 0 || pageEnd > bookPageSize)) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageEnd는 책의 페이지 범위 내에 있어야 합니다."));
            }
            if(pageStart != null && pageEnd != null && pageStart > pageEnd) {
                throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("pageStart는 pageEnd보다 작아야 합니다."));
            }
        }
        if(isPageFilter && isOverview) { // 페이지 필터와 총평보기 필터가 동시에 적용된 경우
            throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("페이지 필터와 총평보기 필터는 동시에 적용될 수 없습니다."));
        }
    }

    public void validateMyRoomPostFilters(Integer pageStart, Integer pageEnd, Boolean isPageFilter, Boolean isOverview, String sort) {
        // 모든 파라미터중 하나라도 null이 아닌 경우 예외 발생
        if (pageStart != null || pageEnd != null || isPageFilter || isOverview || sort != null) {
            throw new BusinessException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("내 기록 조회에서는 roomId, type, cursor를 제외한 모든 파라미터는 null이어야 합니다."));
        }

    }

    public String createBlurredString(String contents) {
        if (contents == null || contents.isEmpty()) {
            return contents;
        }

        int blurLen = BLURRED_STRING.length();
        StringBuilder sb = new StringBuilder(contents.length());

        // 블러 문자열 인덱스
        int blurIndex = 0;

        for (int i = 0; i < contents.length(); i++) {
            char ch = contents.charAt(i);

            // 특수문자/공백일 경우 그대로 append
            if (Character.isWhitespace(ch) || isSpecialCharacter(ch)) {
                sb.append(ch);
            } else {
                // 나머지 문자들은 모두 치환
                sb.append(BLURRED_STRING.charAt(blurIndex));
                blurIndex = (blurIndex + 1) % blurLen; // 순환
            }
        }
        return sb.toString();
    }

    private boolean isSpecialCharacter(char ch) {
        // 아스키 문자 중 문자/숫자만 제외하고 모두 특수문자 처리 예시
        return !Character.isLetterOrDigit(ch);
    }

    public boolean isLocked(int currentPage, int bookPageSize) {
        return currentPage < bookPageSize;
    }
}