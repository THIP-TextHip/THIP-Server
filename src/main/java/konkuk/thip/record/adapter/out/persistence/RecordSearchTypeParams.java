package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RecordSearchTypeParams {
    GROUP("group"),
    MINE("mine");

    private final String value;

    RecordSearchTypeParams(String value) {
        this.value = value;
    }

    public static RecordSearchTypeParams from(String value) {
        return Arrays.stream(RecordSearchTypeParams.values())
                .filter(param -> param.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("type은 group 또는 mine이어야 합니다. 현재 타입 조건 param : " + value))
                );
    }
}
