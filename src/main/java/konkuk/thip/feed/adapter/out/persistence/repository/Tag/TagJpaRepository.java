package konkuk.thip.feed.adapter.out.persistence.repository.Tag;

import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, Long>{
    Optional<TagJpaEntity> findByValue(String value);
}
