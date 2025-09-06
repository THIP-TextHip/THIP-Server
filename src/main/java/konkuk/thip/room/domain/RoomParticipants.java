package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.domain.value.RoomParticipantRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;

@Getter
@RequiredArgsConstructor
public class RoomParticipants {
    /**
     * 특정 Room 과 연관된 UserRoom 들을 모은 일급 컬렉션
     */

    private final List<RoomParticipant> participants;

    public static RoomParticipants from(List<RoomParticipant> participants) {
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
                .anyMatch(userRoom -> userRoom.getRoomParticipantRole().equals(RoomParticipantRole.HOST.getType()));
    }

    public int getCurrentPageOfUser(Long userId) {
        return participants.stream()
                .filter(userRoom -> userRoom.getUserId().equals(userId))
                .map(RoomParticipant::getCurrentPage)
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(ROOM_ACCESS_FORBIDDEN));
    }

    public double getUserPercentageOfUser(Long userId) {
        return participants.stream()
                .filter(userRoom -> userRoom.getUserId().equals(userId))
                .map(RoomParticipant::getUserPercentage)
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(ROOM_ACCESS_FORBIDDEN));
    }
}
