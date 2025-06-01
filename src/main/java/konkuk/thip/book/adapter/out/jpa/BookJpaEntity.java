package konkuk.thip.book.adapter.out.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class BookJpaEntity {

    @Id
    private Long id;
}
