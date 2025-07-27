package konkuk.thip.room.domain;

import konkuk.thip.common.post.CommentCountUpdatable;

public interface RoomPost extends CommentCountUpdatable {
    Long getRoomId();
}