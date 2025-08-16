package konkuk.thip.room.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static konkuk.thip.common.exception.code.ErrorCode.INVALID_ROOM_SEARCH_SORT;

@Getter
@RequiredArgsConstructor
public enum RoomSearchSortParam {

    DEADLINE("deadline"),
    MEMBER_COUNT("memberCount"),
    RECOMMEND("인플루언서, 작가 추천");      // 개발 미정

    private final String value;

    public static RoomSearchSortParam from(String value) {
        return Arrays.stream(RoomSearchSortParam.values())
                .filter(param -> param.getValue().equals(value))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(INVALID_ROOM_SEARCH_SORT)
                );
    }
}
