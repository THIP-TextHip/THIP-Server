package konkuk.thip.comment.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import lombok.*;


@Entity
@Table(
        name = "comment_likes",
        indexes = {
                // 유저별 좋아하는 댓글 목록 복합 인덱스
                @Index(name = "idx_comment_like_user_comment", columnList = "user_id, comment_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommentLikeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentJpaEntity commentJpaEntity;
}