package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.record.adapter.out.persistence.constants.SortType;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecordQueryPersistenceAdapter implements RecordQueryPort {

    private final RecordJpaRepository recordJpaRepository;

    @Override
    public CursorBasedList<PostQueryDto> searchMyRecords(Long roomId, Long userId, Cursor cursor) {
        List<PostQueryDto> postQueryDtos = recordJpaRepository.findMyRecords(roomId, userId, cursor);

        return CursorBasedList.of(postQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.isOverview() ? "1" : "0",
                    postQueryDto.page().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<PostQueryDto> searchGroupRecordsByLatest(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        List<PostQueryDto> postQueryDtos = recordJpaRepository.findGroupRecordsOrderBySortType(
                roomId, userId, cursor, pageStart, pageEnd, isOverview, SortType.CREATED_AT);

        return CursorBasedList.of(postQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.postDate().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<PostQueryDto> searchGroupRecordsByLike(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        List<PostQueryDto> postQueryDtos = recordJpaRepository.findGroupRecordsOrderBySortType(
                roomId, userId, cursor, pageStart, pageEnd, isOverview, SortType.LIKE_COUNT);

        return CursorBasedList.of(postQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.likeCount().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<PostQueryDto> searchGroupRecordsByComment(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        List<PostQueryDto> postQueryDtos = recordJpaRepository.findGroupRecordsOrderBySortType(
                roomId, userId, cursor, pageStart, pageEnd, isOverview, SortType.COMMENT_COUNT);

        return CursorBasedList.of(postQueryDtos, cursor.getPageSize(), postQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(postQueryDto.commentCount().toString(),
                    postQueryDto.postId().toString()));
            return nextCursor.toEncodedString();
        });
    }
}