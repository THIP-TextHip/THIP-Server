package konkuk.thip.room.adapter.out.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RoomJpaEntity {

    @Id
    private Long id;
}
