package konkuk.thip.comment.application.service.policy;

import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.domain.RoomPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomPostCommentAccessPolicy implements CommentAccessPolicy {

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    public void validateCommentAccess(CountUpdatable post, Long userId) {
        RoomPost roomPost = (RoomPost) post;
        roomParticipantValidator.validateUserIsRoomMember(roomPost.getRoomId(), userId);
    }

}