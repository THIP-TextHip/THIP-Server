
package konkuk.thip.feed.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "feeds")
@DiscriminatorValue("FEED")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedJpaEntity extends PostJpaEntity {

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookJpaEntity bookJpaEntity;

    @OneToMany(mappedBy = "postJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentJpaEntity> contentList;

    @Builder
    public FeedJpaEntity(String content, Integer likeCount, Integer commentCount, UserJpaEntity userJpaEntity, Boolean isPublic, int reportCount, BookJpaEntity bookJpaEntity, List<ContentJpaEntity> contentList) {
        super(content, likeCount, commentCount, userJpaEntity);
        this.isPublic = isPublic;
        this.reportCount = reportCount;
        this.bookJpaEntity = bookJpaEntity;
        this.contentList = contentList;
    }
}