package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.PostType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_POST_TYPE_NOT_MATCH;

@Getter
@RequiredArgsConstructor
public enum RoomPostType {

    RECORD("RECORD"),
    VOTE("VOTE");

    private final String type;

    public static RoomPostType from(String type) {
        for (RoomPostType roomPostType : values()) {
            if (roomPostType.getType().equalsIgnoreCase(type)) {
                return roomPostType;
            }
        }
        throw new InvalidStateException(ROOM_POST_TYPE_NOT_MATCH);
    }

    public PostType toPostType() {
        return PostType.from(this.type);
    }
}