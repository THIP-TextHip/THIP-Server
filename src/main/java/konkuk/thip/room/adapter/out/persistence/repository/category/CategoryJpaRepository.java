package konkuk.thip.room.adapter.out.persistence.repository.category;

import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    Optional<CategoryJpaEntity> findByValue(String value);

    // TODO : 리펙토링 대상
    @Query("select a.color " +
            "from CategoryJpaEntity c join c.aliasForCategoryJpaEntity a " +
            "where c.value = :categoryValue")
    Optional<String> findAliasColorByValue(@Param("categoryValue") String categoryValue);
}
