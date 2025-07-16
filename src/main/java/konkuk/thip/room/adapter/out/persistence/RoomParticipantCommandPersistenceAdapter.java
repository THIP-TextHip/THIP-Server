package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.mapper.RoomParticipantMapper;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomParticipantCommandPersistenceAdapter implements RoomParticipantCommandPort {

    private final RoomParticipantJpaRepository roomParticipantJpaRepository;
    private final RoomParticipantMapper roomParticipantMapper;

    @Override
    public RoomParticipant findByUserIdAndRoomId(Long userId, Long roomId) {
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findByUserJpaEntity_UserIdAndRoomJpaEntity_RoomId(userId, roomId).orElseThrow(
                () -> new EntityNotFoundException(ErrorCode.ROOM_PARTICIPANT_NOT_FOUND)
        );

        return roomParticipantMapper.toDomainEntity(roomParticipantJpaEntity);
    }

    @Override
    public List<RoomParticipant> findAllByRoomId(Long roomId) {
        return roomParticipantJpaRepository.findAllByRoomJpaEntity_RoomId(roomId).stream()
                .map(roomParticipantMapper::toDomainEntity)
                .toList();
    }
}
