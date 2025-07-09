package konkuk.thip.room.adapter.out.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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
                        () -> new IllegalArgumentException("현재 정렬 조건 param : " + value)
                );
    }
}
