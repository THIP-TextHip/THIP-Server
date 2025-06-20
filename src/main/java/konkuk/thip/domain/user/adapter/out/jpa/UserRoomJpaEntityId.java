
package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
@Getter
public class UserRoomJpaEntityId implements Serializable {
    private Long userId;
    private Long roomId;
}