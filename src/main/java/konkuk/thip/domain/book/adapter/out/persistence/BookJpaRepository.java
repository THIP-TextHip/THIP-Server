package konkuk.thip.domain.book.adapter.out.persistence;

import konkuk.thip.domain.book.adapter.out.jpa.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long> {
}
