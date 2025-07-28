package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MyRoomType {
    PLAYING("playing"),
    RECRUITING("recruiting"),
    PLAYING_AND_RECRUITING("playingAndRecruiting"),
    EXPIRED("expired");

    private final String type;

    public static MyRoomType from(String type) {
        return Arrays.stream(MyRoomType.values())
                .filter(param -> param.getType().equals(type))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(ErrorCode.INVALID_MY_ROOM_TYPE)
                );
    }
}
