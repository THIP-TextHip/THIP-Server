package konkuk.thip.domain.room.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.domain.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.domain.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.global.entity.BaseJpaEntity;
import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(length = 6100, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    @OneToOne(mappedBy = "postJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private FeedJpaEntity feedJpaEntity;

    @OneToOne(mappedBy = "postJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private VoteJpaEntity voteJpaEntity;

    @OneToOne(mappedBy = "postJpaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private RecordJpaEntity recordJpaEntity;

}