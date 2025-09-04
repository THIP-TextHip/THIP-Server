package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long>, RoomQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<RoomJpaEntity> findByRoomId(Long roomId);

    @Query("SELECT COUNT(r) FROM RoomJpaEntity r " +
            "WHERE r.bookJpaEntity.isbn = :isbn " +
            "AND r.startDate > :currentDate")
    int countActiveRoomsByBookIdAndStartDateAfter(@Param("isbn") String isbn, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT r FROM RoomJpaEntity r WHERE r.roomId IN :roomIds")
    List<RoomJpaEntity> findAllByIds(@Param("roomIds") List<Long> roomIds);
}
