
package konkuk.thip.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.user.domain.value.Alias;
import konkuk.thip.user.domain.User;
import konkuk.thip.user.domain.value.UserRole;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET status = 'INACTIVE' WHERE user_id = ?")
public class UserJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 60, nullable = false)
    private String nickname;

    @Column(name = "nickname_updated_at", nullable = true) // 회원가입 시에는 null
    private LocalDate nicknameUpdatedAt; // 날짜 형식으로 저장 (예: "2023-10-01")

    @Column(name = "oauth2_id", length = 50, nullable = false)
    private String oauth2Id;

    @Builder.Default
    private Integer followerCount = 0; // 팔로워 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Alias alias;

    public void updateIncludeAliasFrom(User user) {
        this.nickname = user.getNickname();
        this.nicknameUpdatedAt = user.getNicknameUpdatedAt();
        this.role = UserRole.from(user.getUserRole());
        this.followerCount = user.getFollowerCount();
        this.alias = user.getAlias();
    }

    public void updateFrom(User user) {
        this.nickname = user.getNickname();
        this.nicknameUpdatedAt = user.getNicknameUpdatedAt();
        this.role = UserRole.from(user.getUserRole());
        this.followerCount = user.getFollowerCount();
    }
}
