package konkuk.thip.roompost.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RoomPostSearchTypeParams {
    GROUP("group"),
    MINE("mine");

    private final String value;

    RoomPostSearchTypeParams(String value) {
        this.value = value;
    }

    public static RoomPostSearchTypeParams from(String value) {
        return Arrays.stream(RoomPostSearchTypeParams.values())
                .filter(param -> param.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("type은 group 또는 mine이어야 합니다. 현재 타입 조건 param : " + value))
                );
    }
}
