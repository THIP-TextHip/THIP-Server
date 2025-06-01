package konkuk.thip.entity;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "following")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Following extends BaseEntity {

    @EmbeddedId
    private FollowingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingUserId")
    @JoinColumn(name = "following_user_id")
    private User followingUser;
}
