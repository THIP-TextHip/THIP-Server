package konkuk.thip.room.application.port.in.dto;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum RoomJoinType {
    JOIN("join"),
    CANCEL("cancel");

    private final String type;

    public static RoomJoinType from(String type) {
        return Arrays.stream(RoomJoinType.values())
                .filter(param -> param.getType().equals(type))
                .findFirst()
                .orElseThrow(
                        () -> new InvalidStateException(ErrorCode.ROOM_JOIN_TYPE_NOT_MATCH)
                );
    }

    public boolean isJoinType() {
        return JOIN.equals(this);
    }
}
