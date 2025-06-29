package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.UserRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoomJpaRepository extends JpaRepository<UserRoomJpaEntity, Long>{
}
