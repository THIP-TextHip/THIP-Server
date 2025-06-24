package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
}
