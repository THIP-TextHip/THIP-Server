package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowingJpaRepository extends JpaRepository<FollowingJpaEntity, Long>, FollowingQueryRepository {


}
