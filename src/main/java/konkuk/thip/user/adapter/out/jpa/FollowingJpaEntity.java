package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(
        name = "followings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_followings_user_target",
                        columnNames = {"user_id", "following_user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FollowingJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "following_id")
    private Long followingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",  nullable = false)
    private UserJpaEntity userJpaEntity; // 팔로잉 하는 유저

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_user_id", nullable = false)
    private UserJpaEntity followingUserJpaEntity; // 팔로우 당하는 유저
}
