package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    Optional<CategoryJpaEntity> findByValue(String value);
}
