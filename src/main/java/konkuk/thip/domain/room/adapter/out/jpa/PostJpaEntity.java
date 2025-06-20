package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.domain.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class PostJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(length = 6100, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    public PostJpaEntity(String content, UserJpaEntity userJpaEntity) {
        this.content = content;
        this.userJpaEntity = userJpaEntity;
    }
}