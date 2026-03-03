package konkuk.thip.book.adapter.out.persistence.repository;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long>, BookQueryRepository {
    Optional<BookJpaEntity> findByIsbn(String isbn);

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

    @Query(
            "SELECT b FROM BookJpaEntity b " +
            "WHERE b.pageCount IS NULL AND b.pageCountUnfindable = false " +
            "AND EXISTS (SELECT r FROM RoomJpaEntity r WHERE r.bookJpaEntity = b) " +
            "ORDER BY b.bookId ASC"
    )
    List<BookJpaEntity> findBooksWithNullPageCountLinkedToRooms();
}
