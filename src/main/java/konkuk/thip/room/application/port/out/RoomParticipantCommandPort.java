package konkuk.thip.room.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.domain.RoomParticipant;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_PARTICIPANT_NOT_FOUND;

public interface RoomParticipantCommandPort {

    Optional<RoomParticipant> findByUserIdAndRoomIdOptional(Long userId, Long roomId);

    default RoomParticipant getByUserIdAndRoomIdOrThrow(Long userId, Long roomId) {
        return findByUserIdAndRoomIdOptional(userId, roomId)
                .orElseThrow(() -> new EntityNotFoundException(ROOM_PARTICIPANT_NOT_FOUND));
    }

    List<RoomParticipant> findAllByRoomId(Long roomId);

    void save(RoomParticipant roomParticipant);

    void deleteByUserIdAndRoomId(Long userId, Long roomId);

    void update(RoomParticipant roomParticipant);

    boolean existsHostUserInActiveRoom(Long userId);

    void deleteAllByUserId(Long userId);

}
