package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.mapper.RoomParticipantMapper;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class RoomParticipantCommandPersistenceAdapter implements RoomParticipantCommandPort {

    private final RoomParticipantJpaRepository roomParticipantJpaRepository;
    private final RoomParticipantMapper roomParticipantMapper;

    private final UserJpaRepository userJpaRepository;
    private final RoomJpaRepository roomJpaRepository;

    @Override
    public List<RoomParticipant> findAllByRoomId(Long roomId) {
        return roomParticipantJpaRepository.findAllByRoomId(roomId).stream()
                .map(roomParticipantMapper::toDomainEntity)
                .toList();
    }

    @Override
    public void save(RoomParticipant roomParticipant) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(roomParticipant.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findByRoomId(roomParticipant.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        roomParticipantJpaRepository.save(roomParticipantMapper.toJpaEntity(
                roomParticipant, userJpaEntity, roomJpaEntity
        ));
    }

    @Override
    public void deleteByUserIdAndRoomId(Long userId, Long roomId) {
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findByUserIdAndRoomId(userId, roomId).orElseThrow(
                () -> new EntityNotFoundException(ErrorCode.ROOM_PARTICIPANT_NOT_FOUND)
        );

        roomParticipantJpaRepository.delete(roomParticipantJpaEntity);
    }

    @Override
    public void update(RoomParticipant roomParticipant) {
        RoomParticipantJpaEntity roomParticipantJpaEntity = roomParticipantJpaRepository.findByRoomParticipantId(roomParticipant.getId()).orElseThrow(
                () -> new EntityNotFoundException(ErrorCode.ROOM_PARTICIPANT_NOT_FOUND)
        );

        roomParticipantJpaEntity.updateFrom(roomParticipant);
        roomParticipantJpaRepository.save(roomParticipantJpaEntity);
    }

    @Override
    public Optional<RoomParticipant> findByUserIdAndRoomIdOptional(Long userId, Long roomId) {
        return roomParticipantJpaRepository.findByUserIdAndRoomId(userId, roomId)
                .map(roomParticipantMapper::toDomainEntity);
    }
}
