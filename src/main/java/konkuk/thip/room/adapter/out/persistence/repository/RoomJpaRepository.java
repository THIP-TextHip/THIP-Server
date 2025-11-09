package konkuk.thip.room.adapter.out.persistence.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long>, RoomQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<RoomJpaEntity> findByRoomId(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomJpaEntity r WHERE r.roomId = :roomId")
    @QueryHints(
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    )
    Optional<RoomJpaEntity> findByRoomIdForUpdate(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(r) FROM RoomJpaEntity r " +
            "WHERE r.bookJpaEntity.isbn = :isbn " +
            "AND r.roomStatus = 'RECRUITING'")
    int countRecruitingRoomsByBookIsbn(@Param("isbn") String isbn);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RoomJpaEntity r
           set r.roomPercentage = :avg,
               r.memberCount    = :count
           where r.roomId = :roomId
        """)
    void updateRoomStats(@Param("roomId") Long roomId, @Param("avg") double avg, @Param("count") int count);
    /**
     * end_date < 오늘 => EXPIRED
     * 이미 EXPIRED 인 것은 제외
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RoomJpaEntity r
              set r.roomStatus = 'EXPIRED'
            where r.endDate < current_date
              and r.roomStatus <> 'EXPIRED'
           """)
    int updateRoomStatusToExpired();

    /**
     * start_date <= 오늘 AND end_date >= 오늘 => IN_PROGRESS
     * RECRUITING 인 것만 대상
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RoomJpaEntity r
              set r.roomStatus = 'IN_PROGRESS'
            where r.startDate <= current_date
              and r.endDate >= current_date
              and r.roomStatus = 'RECRUITING'
           """)
    int updateRoomStatusFromRecruitingToProgress();

    @Query("""
           select r
             from RoomJpaEntity r
            where r.startDate <= current_date
              and r.endDate   >= current_date
              and r.roomStatus = 'RECRUITING'
           """)
    List<RoomJpaEntity> findProgressTargetIds();
}
