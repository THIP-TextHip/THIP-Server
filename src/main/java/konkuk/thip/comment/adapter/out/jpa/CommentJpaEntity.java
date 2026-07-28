package konkuk.thip.comment.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.post.domain.PostType;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_post_parent", columnList = "post_id, parent_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE comments SET status = 'INACTIVE' WHERE comment_id = ?")
public class CommentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(length = 650, nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;

    /**
     * -- SETTER --
     *  회원 탈퇴용
     */
    @Setter
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    /**
     * 루트 댓글의 자식 댓글 수 (루트 댓글만 사용)
     * 자식 댓글인 경우 항상 0
     */
    @Builder.Default
    @Column(name = "descendant_count", nullable = false)
    private int descendantCount = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostJpaEntity postJpaEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 10)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    /**
     * nullable = true : 최상위 댓글인 경우 null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentJpaEntity parent;

    /**
     * 루트 댓글 참조 (모든 자손 댓글이 루트를 직접 참조)
     * 루트 댓글인 경우: null (nullable)
     * 자식 댓글인 경우: 최상위 루트 댓글
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_comment_id")
    private CommentJpaEntity root;

    public CommentJpaEntity updateFrom(Comment comment) {
        this.reportCount = comment.getReportCount();
        this.likeCount = comment.getLikeCount();
        this.status = comment.getStatus();
        return this;
    }

    @VisibleForTesting
    public void updateLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * 자식 댓글 수 증가 (루트 댓글만 호출)
     */
    public void incrementDescendantCount() {
        this.descendantCount++;
    }

    /**
     * 자식 댓글 수 감소 (루트 댓글만 호출)
     */
    public void decrementDescendantCount() {
        if (this.descendantCount > 0) {
            this.descendantCount--;
        }
    }
}
