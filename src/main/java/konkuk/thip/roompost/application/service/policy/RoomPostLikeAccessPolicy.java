package konkuk.thip.roompost.application.service.policy;

import konkuk.thip.post.application.service.policy.PostLikeAccessPolicy;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.domain.RoomPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomPostLikeAccessPolicy implements PostLikeAccessPolicy {

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    public void validatePostLikeAccess(CountUpdatable post, Long userId) {
        RoomPost roomPost = (RoomPost) post;
        roomParticipantValidator.validateUserIsRoomMember(roomPost.getRoomId(), userId);
    }
}