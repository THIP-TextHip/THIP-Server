package konkuk.thip.comment.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
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
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommentJpaEntity commentJpaEntity;
}