package konkuk.thip.roompost.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RoomPostSearchSortParams {

    LATEST("latest"),
    LIKE("like"),
    COMMENT("comment");

    private final String value;

    RoomPostSearchSortParams(String value) {
        this.value = value;
    }

    public static RoomPostSearchSortParams from(String value) {
        return Arrays.stream(RoomPostSearchSortParams.values())
                .filter(param -> param.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("현재 정렬 조건 param : " + value))
                );
    }
}
