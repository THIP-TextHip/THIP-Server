package konkuk.thip.roompost.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.persistence.repository.CommentJpaRepository;
import konkuk.thip.comment.adapter.out.persistence.repository.CommentLikeJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.post.adapter.out.persistence.PostLikeJpaRepository;
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

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class RecordCommandPersistenceAdapter implements RecordCommandPort {

    private final RecordJpaRepository recordJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RoomJpaRepository roomJpaRepository;

    private final CommentJpaRepository commentJpaRepository;
    private final CommentLikeJpaRepository commentLikeJpaRepository;
    private final PostLikeJpaRepository postLikeJpaRepository;

    private final RecordMapper recordMapper;

    @Override
    public Long save(Record record) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(record.getCreatorId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        RoomJpaEntity roomJpaEntity = roomJpaRepository.findByRoomId(record.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        return recordJpaRepository.save(
                recordMapper.toJpaEntity(record, userJpaEntity, roomJpaEntity)
        ).getPostId();
    }

    @Override
    public Optional<Record> findById(Long id) {
        return recordJpaRepository.findByPostId(id)
                .map(recordMapper::toDomainEntity);
    }

    @Override
    public void delete(Record record) {
        RecordJpaEntity recordJpaEntity = recordJpaRepository.findByPostId(record.getId()).orElseThrow(
                () -> new EntityNotFoundException(RECORD_NOT_FOUND)
        );

        recordJpaEntity.softDelete();
        recordJpaRepository.save(recordJpaEntity);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        // 1. 유저가 작성한 피드 게시글 ID 리스트 조회
        List<Long> recordIds = recordJpaRepository.findRecordIdsByUserId(userId);
        if (recordIds == null || recordIds.isEmpty()) {
            return; // early return
        }
        // 2-1. 댓글 좋아요 일괄 삭제
        commentLikeJpaRepository.deleteAllByPostIds(recordIds);
        // 2-2. 댓글 soft delete 일괄 처리
        commentJpaRepository.softDeleteAllByPostIds(recordIds);
        // 3. 게시글 좋아요 일괄 삭제
        postLikeJpaRepository.deleteAllByPostIds(recordIds);
        // 4. 탈퇴한 유저가 작성한 기록 게시글 일괄 삭제
        recordJpaRepository.deleteAllByUserId(userId);
    }

    @Override
    public void update(Record record) {
        RecordJpaEntity recordJpaEntity = recordJpaRepository.findByPostId(record.getId()).orElseThrow(
                () -> new EntityNotFoundException(RECORD_NOT_FOUND)
        );

        recordJpaRepository.save(recordJpaEntity.updateFrom(record));
    }

}
