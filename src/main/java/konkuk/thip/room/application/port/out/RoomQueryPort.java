package konkuk.thip.room.application.port.out;

import java.time.LocalDate;

public interface RoomQueryPort {
    int countRecruitingRoomsByBookAndStartDateAfter(Long bookId, LocalDate currentDate);
}
