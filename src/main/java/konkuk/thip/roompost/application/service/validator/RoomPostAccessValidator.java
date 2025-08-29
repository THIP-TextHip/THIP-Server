package konkuk.thip.roompost.application.service.validator;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;

@HelperService
public class RoomPostAccessValidator {

    private static final String BLURRED_STRING = "여긴 못 지나가지롱~~";

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

        int originalLength = contents.length();
        int blurLen = BLURRED_STRING.length();

        // 필요한 전체 반복 횟수 계산
        int repeat = originalLength / blurLen;

        StringBuilder sb = new StringBuilder(originalLength);

        // 몫 만큼 반복
        for (int i = 0; i < repeat + 1; i++) {
            sb.append(BLURRED_STRING);
        }

        return sb.toString();
    }

    public boolean isLocked(int currentPage, int bookPageSize) {
        return currentPage < bookPageSize;
    }
}