
package konkuk.thip.feed.adapter.out.jpa;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.*;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.converter.ContentListJsonConverter;
import konkuk.thip.feed.adapter.out.jpa.converter.TagListJsonConverter;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.value.TagList;
import konkuk.thip.feed.domain.value.ContentList;
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
    @JoinColumn(name = "book_id")   // RECORD, VOTE 로 인해 nullable = true로 설정
    private BookJpaEntity bookJpaEntity;

    // JSON 문자열로 저장되는 단일 컬럼
    @Convert(converter = ContentListJsonConverter.class)
    @Column(name = "content_list", columnDefinition = "TEXT")
    private ContentList contentList = ContentList.empty();

    // 삭제용 피드 저장 양방향 매핑 관계
    @OneToMany(mappedBy = "feedJpaEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SavedFeedJpaEntity> savedFeeds = new ArrayList<>();

    @Column(name = "tag_list", columnDefinition = "TEXT")
    @Convert(converter = TagListJsonConverter.class)
    private TagList tagList = TagList.empty();

    @Builder
    public FeedJpaEntity(String content, Integer likeCount, Integer commentCount, UserJpaEntity userJpaEntity, Boolean isPublic, int reportCount, BookJpaEntity bookJpaEntity, ContentList contentList, TagList tagList) {
        super(content, likeCount, commentCount, userJpaEntity);
        this.isPublic = isPublic;
        this.reportCount = reportCount;
        this.bookJpaEntity = bookJpaEntity;
        this.contentList = contentList != null ? contentList : ContentList.empty();
        this.tagList = tagList != null ? tagList : TagList.empty();
    }

    public void updateFrom(Feed feed) {
        this.content = feed.getContent();
        this.isPublic = feed.getIsPublic();
        this.reportCount = feed.getReportCount();
        this.likeCount = feed.getLikeCount();
        this.commentCount = feed.getCommentCount();
        this.contentList = feed.getContentList();
        this.tagList = feed.getTagList();
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