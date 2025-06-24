package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {
}
