package konkuk.thip.book.adapter.out.persistence.adapter;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long> {
    Optional<BookJpaEntity> findByIsbn(String isbn);
    List<BookJpaEntity> findByIsbnIn(List<String> isbnList);
}
