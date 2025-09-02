package konkuk.thip.roompost.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.roompost.domain.Vote;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("VOTE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteJpaEntity extends PostJpaEntity {

    @Column(name = "page")
    private Integer page;

    @Column(name = "is_overview")
    private boolean isOverview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")   // FEED 로 인해 nullable = true로 설정
    private RoomJpaEntity roomJpaEntity;

    @Builder
    public VoteJpaEntity(String content, Integer likeCount, Integer commentCount, UserJpaEntity userJpaEntity, Integer page, boolean isOverview, RoomJpaEntity roomJpaEntity) {
        super(content, likeCount, commentCount, userJpaEntity);
        this.page = page;
        this.isOverview = isOverview;
        this.roomJpaEntity = roomJpaEntity;
    }

    public VoteJpaEntity updateFrom(Vote vote) {
        this.content = vote.getContent();
        this.likeCount = vote.getLikeCount();
        this.commentCount = vote.getCommentCount();
        this.page = vote.getPage();
        this.isOverview = vote.isOverview();
        return this;
    }

    @VisibleForTesting
    public void updateLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @VisibleForTesting
    public void updateCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}