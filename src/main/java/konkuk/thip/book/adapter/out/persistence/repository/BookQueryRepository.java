package konkuk.thip.book.adapter.out.persistence.repository;

import konkuk.thip.book.application.port.out.dto.BookQueryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface BookQueryRepository {
    List<BookQueryDto> findSavedBooksBySavedAt(Long userId, LocalDateTime lastSavedAt, int size);

    List<BookQueryDto> findJoiningRoomsBooksByRoomPercentage(Long userId, Double lastRoomPercentage, Long lastBookId, int pageSize);
}
