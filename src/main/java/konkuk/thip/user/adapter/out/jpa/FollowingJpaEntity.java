package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "followings")
@Getter
@SQLDelete(sql = "UPDATE followings SET status = 'INACTIVE' WHERE following_id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FollowingJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "following_id")
    private Long followingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity followerUserJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_user_id")
    private UserJpaEntity followingUserJpaEntity;
}
