package konkuk.thip.roompost.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.roompost.adapter.out.mapper.RecordMapper;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.entity.StatusType.ACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class RecordCommandPersistenceAdapter implements RecordCommandPort {

    private final RecordJpaRepository recordJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoomJpaRepository roomJpaRepository;
    private final RecordMapper recordMapper;

    @Override
    public Long saveRecord(Record record) {
        UserJpaEntity userJpaEntity = userJpaRepository.findById(record.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(record.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        return recordJpaRepository.save(
                recordMapper.toJpaEntity(record, userJpaEntity, roomJpaEntity)
        ).getPostId();
    }

    @Override
    public Optional<Record> findById(Long id) {
        return recordJpaRepository.findByPostIdAndStatus(id, ACTIVE)
                .map(recordMapper::toDomainEntity);
    }

    @Override
    public void delete(Record record) {
        RecordJpaEntity recordJpaEntity = recordJpaRepository.findByPostIdAndStatus(record.getId(),ACTIVE).orElseThrow(
                () -> new EntityNotFoundException(RECORD_NOT_FOUND)
        );

        recordJpaEntity.softDelete();
        recordJpaRepository.save(recordJpaEntity);
    }

    @Override
    public void update(Record record) {
        RecordJpaEntity recordJpaEntity = recordJpaRepository.findByPostIdAndStatus(record.getId(),ACTIVE).orElseThrow(
                () -> new EntityNotFoundException(RECORD_NOT_FOUND)
        );

        recordJpaRepository.save(recordJpaEntity.updateFrom(record));
    }

}
