package konkuk.thip.roompost.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.roompost.adapter.out.mapper.RecordMapper;
import konkuk.thip.roompost.adapter.out.persistence.repository.record.RecordJpaRepository;
import konkuk.thip.roompost.application.port.out.RecordQueryPort;
import konkuk.thip.roompost.application.port.out.dto.RoomPostQueryDto;
import konkuk.thip.roompost.domain.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecordQueryPersistenceAdapter implements RecordQueryPort {

    private final RecordJpaRepository recordJpaRepository;
    private final RecordMapper recordMapper;

    @Override
    public CursorBasedList<RoomPostQueryDto> searchMyRecords(Long roomId, Long userId, Cursor cursor) {
        List<RoomPostQueryDto> roomPostQueryDtos = recordJpaRepository.findMyRecords(roomId, userId, cursor);

        return CursorBasedList.of(roomPostQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.isOverview() ? "1" : "0",
                    postQueryDto.page().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<RoomPostQueryDto> searchGroupRecordsByLatest(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        List<RoomPostQueryDto> roomPostQueryDtos = recordJpaRepository.findGroupRecordsOrderBySortType(
                roomId, userId, cursor, pageStart, pageEnd, isOverview, RoomPostSortType.CREATED_AT);

        return CursorBasedList.of(roomPostQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.postDate().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<RoomPostQueryDto> searchGroupRecordsByLike(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        List<RoomPostQueryDto> roomPostQueryDtos = recordJpaRepository.findGroupRecordsOrderBySortType(
                roomId, userId, cursor, pageStart, pageEnd, isOverview, RoomPostSortType.LIKE_COUNT);

        return CursorBasedList.of(roomPostQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.likeCount().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<RoomPostQueryDto> searchGroupRecordsByComment(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        List<RoomPostQueryDto> roomPostQueryDtos = recordJpaRepository.findGroupRecordsOrderBySortType(
                roomId, userId, cursor, pageStart, pageEnd, isOverview, RoomPostSortType.COMMENT_COUNT);

        return CursorBasedList.of(roomPostQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.commentCount().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public List<Record> findAllByRoomIdAndUserId(Long roomId, Long userId) {
        return recordJpaRepository.findAllByRoomIdAndUserIdOrderByPageAsc(roomId, userId).stream()
                .map(recordMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Integer countAllByRoomIdAndUserId(Long roomId, Long userId) {
        return recordJpaRepository.countAllByRoomIdAndUserId(roomId, userId);
    }
}