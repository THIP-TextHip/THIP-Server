package konkuk.thip.book.adapter.out.persistence.repository;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long> {
    Optional<BookJpaEntity> findByIsbn(String isbn);

    @Query("SELECT b FROM BookJpaEntity b " +
            "JOIN SavedBookJpaEntity s ON s.bookJpaEntity.bookId = b.bookId " +
            "WHERE s.userJpaEntity.userId = :userId " +
            "GROUP BY b " +
            "ORDER BY MAX(s.createdAt) DESC")
    List<BookJpaEntity> findSavedBooksByUserId(Long userId);

    @Query("SELECT b FROM BookJpaEntity b " +
            "JOIN RoomJpaEntity r ON r.bookJpaEntity.bookId = b.bookId " +
            "JOIN RoomParticipantJpaEntity rp ON rp.roomJpaEntity.roomId = r.roomId " +
            "WHERE rp.userJpaEntity.userId = :userId " +
            "AND r.startDate <= CURRENT_TIMESTAMP " + // 진행 중인 방만 조회 (모집 중 / 만료된 방 x)
            "GROUP BY b " +
            "ORDER BY MAX(r.roomPercentage) DESC") // 방의 진행률이 높은 순서로 정렬
    List<BookJpaEntity> findJoiningRoomsBooksByUserId(Long userId);

    boolean existsByIsbn(String isbn);

    // Room, Feed, SavedBook에 모두 참조되지 않은 책 ID만 찾는 쿼리
    @Query(
            "SELECT b.bookId " +
            "FROM BookJpaEntity b " +
            "WHERE NOT EXISTS ( " +
            "    SELECT 1 FROM RoomJpaEntity r " +
            "    WHERE r.bookJpaEntity = b " +
            ") " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM FeedJpaEntity f " +
            "    WHERE f.bookJpaEntity = b " +
            ") " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM SavedBookJpaEntity s " +
            "    WHERE s.bookJpaEntity = b " +
            ")"
    )
    Set<Long> findUnusedBookIds();
}
