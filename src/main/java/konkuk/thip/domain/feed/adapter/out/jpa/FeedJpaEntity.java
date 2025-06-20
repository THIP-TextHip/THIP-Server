
package konkuk.thip.domain.feed.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.domain.room.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedJpaEntity extends BaseJpaEntity {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Builder.Default
    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostJpaEntity postJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookJpaEntity bookJpaEntity;
}