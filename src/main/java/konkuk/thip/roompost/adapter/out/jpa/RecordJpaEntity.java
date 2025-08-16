package konkuk.thip.roompost.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "records")
@DiscriminatorValue("RECORD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordJpaEntity extends PostJpaEntity {

    private Integer page;

    @Column(name = "is_overview",nullable = false)
    private boolean isOverview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity roomJpaEntity;

    @Builder
    public RecordJpaEntity(String content, Integer likeCount, Integer commentCount, UserJpaEntity userJpaEntity, Integer page, boolean isOverview, RoomJpaEntity roomJpaEntity) {
        super(content, likeCount, commentCount, userJpaEntity);
        this.page = page;
        this.isOverview = isOverview;
        this.roomJpaEntity = roomJpaEntity;
    }

    public RecordJpaEntity updateFrom(Record record) {
        this.content = record.getContent();
        this.likeCount = record.getLikeCount();
        this.commentCount = record.getCommentCount();
        this.page = record.getPage();
        this.isOverview = record.isOverview();
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

