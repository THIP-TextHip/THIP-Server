package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FollowingJpaEntityId implements Serializable {
    private Long userId;
    private Long followingUserId;
}