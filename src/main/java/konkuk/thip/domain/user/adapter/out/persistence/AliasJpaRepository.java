package konkuk.thip.domain.user.adapter.out.persistence;

import konkuk.thip.domain.user.adapter.out.jpa.AliasJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AliasJpaRepository extends JpaRepository<AliasJpaEntity, Long> {
}