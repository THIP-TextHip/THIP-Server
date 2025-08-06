package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.PostType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_POST_TYPE_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum RoomPostType {

    RECORD("RECORD"),
    VOTE("VOTE");

    private final String type;

    public static RoomPostType from(String type) {
        return Arrays.stream(RoomPostType.values())
                .filter(p -> p.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() ->
                        new InvalidStateException(ROOM_POST_TYPE_NOT_MATCH)
                );
    }

    public PostType toPostType() {
        return PostType.from(this.type);
    }
}