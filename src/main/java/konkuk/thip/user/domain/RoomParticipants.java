package konkuk.thip.user.domain;

import konkuk.thip.user.adapter.out.jpa.UserRoomRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RoomParticipants {
    /**
     * 특정 Room 과 연관된 UserRoom 들을 모은 일급 컬렉션
     */

    private final List<UserRoom> participants;

    public static RoomParticipants from(List<UserRoom> participants) {
        return new RoomParticipants(participants);
    }

    public int calculateMemberCount() {
        return participants.size();
    }

    public boolean isJoiningToRoom(Long userId) {
        return participants.stream()
                .anyMatch(userRoom -> userRoom.getUserId().equals(userId));
    }

    public boolean isHostOfRoom(Long userId) {
        return participants.stream()
                .filter(userRoom -> userRoom.getUserId().equals(userId))
                .anyMatch(userRoom -> userRoom.getUserRoomRole().equals(UserRoomRole.HOST.getType()));
    }
}
