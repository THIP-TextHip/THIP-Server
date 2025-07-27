package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.room.application.service.RoomParticipantValidator;
import konkuk.thip.room.domain.RoomPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomPostCommentAccessPolicy implements CommentAccessPolicy {

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    public void validateCommentAccess(CommentCountUpdatable post, Long userId) {
        RoomPost roomPost = (RoomPost) post;
        roomParticipantValidator.validateUserIsRoomMember(roomPost.getRoomId(), userId);
    }

}