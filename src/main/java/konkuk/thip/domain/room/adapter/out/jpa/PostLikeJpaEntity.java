package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.global.entity.BaseJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;

@Entity
@Table(name = "post_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostLikeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostJpaEntity postJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;
}