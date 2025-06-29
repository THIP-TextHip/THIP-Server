package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class RoomQueryPersistenceAdapter implements RoomQueryPort {

    private final RoomJpaRepository roomJpaRepository;
    private final RoomMapper roomMapper;

    @Override
    public int countRecruitingRoomsByBookAndStartDateAfter(Long bookId, LocalDate currentDate) {
        return roomJpaRepository.countByBookJpaEntity_BookIdAndStartDateAfter(bookId, currentDate);
    }
}
