package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserJpaEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String email;

    private String password;
}
