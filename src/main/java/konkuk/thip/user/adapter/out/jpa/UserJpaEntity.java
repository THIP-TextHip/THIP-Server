package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserJpaEntity {

    @Id
    private Long id;
}
