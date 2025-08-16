package konkuk.thip.feed.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedRelatedWithBookSortType {
    LIKE("like"),
    LATEST("latest")
    ;

    private final String type;

    public static FeedRelatedWithBookSortType from(String type) {
        for (FeedRelatedWithBookSortType sortType : values()) {
            if (sortType.getType().equals(type)) {
                return sortType;
            }
        }
        throw new InvalidStateException(ErrorCode.API_INVALID_PARAM,
                new IllegalArgumentException("정렬 타입은 like 또는 latest 중 하나여야 합니다. 현재 타입: " + type));
    }
}
