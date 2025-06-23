
package konkuk.thip.domain.user.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.room.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_alias_id", nullable = false)
    private AliasJpaEntity aliasForUserJpaEntity;

}