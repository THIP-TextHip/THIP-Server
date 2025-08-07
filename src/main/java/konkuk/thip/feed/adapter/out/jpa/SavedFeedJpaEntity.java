package konkuk.thip.feed.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;

@Entity
@Table(name = "saved_feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SavedFeedJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private FeedJpaEntity feedJpaEntity;
}