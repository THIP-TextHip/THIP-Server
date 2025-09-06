package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.domain.value.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * end_date < 오늘 => EXPIRED
     * 이미 EXPIRED 인 것은 제외
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RoomJpaEntity r
              set r.roomStatus = :exceptStatus
            where r.endDate < current_date
              and r.roomStatus <> :exceptStatus
           """)
    int updateRoomStatusToExpired(@Param("exceptStatus") RoomStatus exceptStatus);

    /**
     * start_date <= 오늘 AND end_date >= 오늘 => IN_PROGRESS
     * RECRUITING 인 것만 대상
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RoomJpaEntity r
              set r.roomStatus = :toStatus
            where r.startDate <= current_date
              and r.endDate >= current_date
              and r.roomStatus = :fromStatus
           """)
    int updateRoomStatus(@Param("fromStatus") RoomStatus fromStatus, @Param("toStatus") RoomStatus toStatus);

    @Query("""
           select r
             from RoomJpaEntity r
            where r.startDate <= current_date
              and r.endDate   >= current_date
              and r.roomStatus = :status
           """)
    List<RoomJpaEntity> findProgressTargetIds(@Param("status") RoomStatus status);
}
