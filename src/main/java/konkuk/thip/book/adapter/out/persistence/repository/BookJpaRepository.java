package konkuk.thip.book.adapter.out.persistence.repository;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long>,BookQueryRepository {
    Optional<BookJpaEntity> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}
