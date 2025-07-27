package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;
import konkuk.thip.room.application.port.out.RoomParticipantQueryPort;
import konkuk.thip.room.application.service.RoomParticipantValidator;
import konkuk.thip.room.domain.RoomPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;

@Component
@RequiredArgsConstructor
public class RoomPostCommentAccessPolicy implements CommentAccessPolicy {

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    public boolean supports(PostType type) {
        return type == PostType.RECORD || type == PostType.VOTE;
    }

    @Override
    public void validateCommentAccess(CommentCountUpdatable post, Long userId) {
        RoomPost roomPost = (RoomPost) post;
        roomParticipantValidator.validateUserIsRoomMember(roomPost.getRoomId(), userId);
    }
}
