package konkuk.thip.book.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public enum BookSelectableType {
    SAVED("SAVED"),  // 저장된 책
    JOINING("JOINING") // 참여 중인 모임 방의 책
    ;

    private final String type;

    BookSelectableType(String type) {
        this.type = type;
    }

    public static BookSelectableType from(String type) {
        for (BookSelectableType bookSelectableType : BookSelectableType.values()) {
            if (bookSelectableType.type.equals(type)) {
                return bookSelectableType;
            }
        }
        throw new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("타입은 SAVED 또는 JOINING이어야 합니다. 현재 타입: " + type));
    }
}
