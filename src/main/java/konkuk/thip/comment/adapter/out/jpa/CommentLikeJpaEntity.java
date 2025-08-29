package konkuk.thip.comment.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;


@Entity
@Table(name = "comment_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommentLikeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentJpaEntity commentJpaEntity;
}