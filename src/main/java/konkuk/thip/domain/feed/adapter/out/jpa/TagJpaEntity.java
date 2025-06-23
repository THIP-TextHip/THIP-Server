package konkuk.thip.domain.feed.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.book.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.domain.room.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import lombok.*;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TagJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_value",length = 50, nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostJpaEntity postJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity categoryJpaEntity;
}