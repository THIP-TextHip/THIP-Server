package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RecordSearchSortParams {

    LATEST("latest"),
    LIKE("like"),
    COMMENT("comment");

    private final String value;

    RecordSearchSortParams(String value) {
        this.value = value;
    }

    public static RecordSearchSortParams from(String value) {
        return Arrays.stream(RecordSearchSortParams.values())
                .filter(param -> param.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(ErrorCode.API_INVALID_PARAM, new IllegalArgumentException("현재 정렬 조건 param : " + value))
                );
    }
}
