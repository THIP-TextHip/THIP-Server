package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AliasJpaRepository extends JpaRepository<AliasJpaEntity, Long>, AliasQueryRepository {
}