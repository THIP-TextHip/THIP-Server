package konkuk.thip.post.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static konkuk.thip.common.entity.StatusType.INACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.POST_ALREADY_DELETED;

@Entity
@Table(name = "posts")
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PostJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(length = 6100, nullable = false)
    protected String content;

    protected Integer likeCount = 0;

    protected Integer commentCount = 0;

    // type 구분을 위한 조회용 컬럼
    @Column(name = "dtype", insertable = false, updatable = false)
    private String dtype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    // 삭제용 게시물 댓글 양방향 매핑 관계
    @OneToMany(mappedBy = "postJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommentJpaEntity> commentList = new ArrayList<>();

    // 삭제용 게시물 좋아요 양방향 매핑 관계
    @OneToMany(mappedBy = "postJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostLikeJpaEntity> postLikeList = new ArrayList<>();

    public PostJpaEntity(String content, Integer likeCount, Integer commentCount, UserJpaEntity userJpaEntity) {
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.userJpaEntity = userJpaEntity;
    }

    public void softDelete() {
        if(this.status.equals(INACTIVE)){
            throw new InvalidStateException(POST_ALREADY_DELETED);
        }
        this.status = INACTIVE;
    }
}