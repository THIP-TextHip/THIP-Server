package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AttendanceCheckJpaEntityId implements Serializable {
    private Long roomId;
    private Long userId;
}