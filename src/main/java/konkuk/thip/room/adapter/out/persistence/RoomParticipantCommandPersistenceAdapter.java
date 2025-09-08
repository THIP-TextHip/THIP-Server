package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.mapper.RoomParticipantMapper;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.adapter.out.persistence.projection.RoomAggregateProjection;
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
    public boolean existsHostUserInActiveRoom(Long userId) {
        return roomParticipantJpaRepository.existsHostUserInActiveRoom(userId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        // 방 참여 관계 삭제 (member는 진행/모집/만료, host는 만료)
        // 방 멤버수 감소, 방 진행도 업데이트

        // 1. 유저가 참여한 방 ID 리스트 조회
        List<Long> roomIds = roomParticipantJpaRepository.findRoomIdsByUserId(userId);
        if (roomIds.isEmpty()) {
            return; // early return
        }
        // 2. 유저의 모든 방 참여 관계 일괄 삭제
        roomParticipantJpaRepository.softDeleteAllByUserId(userId);

        // 3. 남은 ACTIVE 참여자 기준 방별 평균/인원 집계
        List<RoomAggregateProjection> stats = roomParticipantJpaRepository.aggregateStatsByRoomIds(roomIds);

        // 4. 방 정보(진행률, 멤버수) 업데이트
        for (RoomAggregateProjection row : stats) {
            roomJpaRepository.updateRoomStats(
                    row.getRoomId(),
                    row.getAvgPercentage() == null ? 0.0 : row.getAvgPercentage(),
                    row.getMemberCount().intValue()
            );
        }
    }

    @Override
    public Optional<RoomParticipant> findByUserIdAndRoomIdOptional(Long userId, Long roomId) {
        return roomParticipantJpaRepository.findByUserIdAndRoomId(userId, roomId)
                .map(roomParticipantMapper::toDomainEntity);
    }
}
