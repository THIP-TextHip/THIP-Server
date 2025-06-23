
package konkuk.thip.domain.feed.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.domain.room.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostJpaEntity postJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookJpaEntity bookJpaEntity;

    @Builder
    public FeedJpaEntity(String content, UserJpaEntity userJpaEntity, Boolean isPublic, BookJpaEntity bookJpaEntity) {
        super(content, userJpaEntity);
        this.isPublic = isPublic;
        this.reportCount = 0; // Default 값
        this.bookJpaEntity = bookJpaEntity;
    }
}