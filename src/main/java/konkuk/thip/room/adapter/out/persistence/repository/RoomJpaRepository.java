package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RoomJpaEntity r
           set r.roomPercentage = :avg,
               r.memberCount    = :count
           where r.roomId = :roomId
        """)
    void updateRoomStats(@Param("roomId") Long roomId, @Param("avg") double avg, @Param("count") int count);
}
