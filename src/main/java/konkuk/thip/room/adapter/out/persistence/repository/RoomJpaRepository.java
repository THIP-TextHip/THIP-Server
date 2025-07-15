package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long>, RoomQueryRepository {

    int countByBookJpaEntity_BookIdAndStartDateAfter(Long bookId, LocalDate currentDate);


}
