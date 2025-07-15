package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AliasJpaRepository extends JpaRepository<AliasJpaEntity, Long>, AliasQueryRepository {

    Optional<AliasJpaEntity> findByValue(String value);
}