package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.user.adapter.out.jpa.UserRoomRole;
import konkuk.thip.user.domain.UserRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_BELONG_TO_ROOM;

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

    public int getCurrentPageOfUser(Long userId) {
        return participants.stream()
                .filter(userRoom -> userRoom.getUserId().equals(userId))
                .map(UserRoom::getCurrentPage)
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(USER_NOT_BELONG_TO_ROOM));
    }

    public double getUserPercentageOfUser(Long userId) {
        return participants.stream()
                .filter(userRoom -> userRoom.getUserId().equals(userId))
                .map(UserRoom::getUserPercentage)
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(USER_NOT_BELONG_TO_ROOM));
    }
}
