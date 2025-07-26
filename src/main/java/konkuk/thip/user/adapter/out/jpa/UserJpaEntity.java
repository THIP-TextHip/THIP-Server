
package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.user.domain.User;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 60, nullable = false)
    private String nickname;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(name = "oauth2_id", length = 50, nullable = false)
    private String oauth2Id;

    @Builder.Default
    private Integer followerCount = 0; // 팔로워 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_alias_id", nullable = false)
    private AliasJpaEntity aliasForUserJpaEntity;

    public void updateFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public void updateFrom(User user) {
        this.nickname = user.getNickname();
        this.imageUrl = user.getAlias().getImageUrl();
        this.role = UserRole.from(user.getUserRole());
        this.followerCount = user.getFollowerCount();
    }
}