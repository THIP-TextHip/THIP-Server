package konkuk.thip.comment.domain.policy;

import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;
import konkuk.thip.room.domain.service.RoomParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomPostCommentAccessPolicy implements CommentAccessPolicy {

    private final RoomParticipantService roomParticipantService;

    @Override
    public boolean supports(PostType type) {
        return type == PostType.RECORD || type == PostType.VOTE;
    }

    @Override
    public void validateCommentAccess(CommentCountUpdatable post, Long userId) {
        roomParticipantService.validateUserIsRoomMember(post.getRoomId(), userId);
    }
}
