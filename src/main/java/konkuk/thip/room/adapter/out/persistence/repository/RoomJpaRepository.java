package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long>, RoomQueryRepository {

    @Query("SELECT COUNT(r) FROM RoomJpaEntity r " +
            "WHERE r.bookJpaEntity.bookId = :bookId " +
            "AND r.startDate > :currentDate " +
            "AND r.status = 'ACTIVE'")
    int countActiveRoomsByBookIdAndStartDateAfter(@Param("bookId") Long bookId, @Param("currentDate") LocalDate currentDate);

}
