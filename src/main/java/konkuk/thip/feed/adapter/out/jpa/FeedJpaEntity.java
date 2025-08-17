
package konkuk.thip.feed.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
//@Table(name = "feeds")
@DiscriminatorValue("FEED")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedJpaEntity extends PostJpaEntity {

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "report_count")
    private int reportCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookJpaEntity bookJpaEntity;

    @OneToMany(mappedBy = "postJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentJpaEntity> contentList;

    // 삭제용 피드 저장 양방향 매핑 관계
    @OneToMany(mappedBy = "feedJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SavedFeedJpaEntity> savedFeeds = new ArrayList<>();

    // 삭제용 피드 태그 양방향 매핑 관계
    @OneToMany(mappedBy = "feedJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FeedTagJpaEntity> feedTags = new ArrayList<>();

    @Builder
    public FeedJpaEntity(String content, Integer likeCount, Integer commentCount, UserJpaEntity userJpaEntity, Boolean isPublic, int reportCount, BookJpaEntity bookJpaEntity, List<ContentJpaEntity> contentList) {
        super(content, likeCount, commentCount, userJpaEntity);
        this.isPublic = isPublic;
        this.reportCount = reportCount;
        this.bookJpaEntity = bookJpaEntity;
        this.contentList = contentList;
    }

    public void updateFrom(Feed feed) {
        this.content = feed.getContent();
        this.isPublic = feed.getIsPublic();
        this.reportCount = feed.getReportCount();
        this.likeCount = feed.getLikeCount();
        this.commentCount = feed.getCommentCount();
    }

    @VisibleForTesting
    public void updateCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @VisibleForTesting
    public void updateLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

}