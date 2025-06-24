package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.user.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.SavedFeed;
import org.springframework.stereotype.Component;

@Component
public class SavedFeedMapper {

    public SavedFeedJpaEntity toJpaEntity(UserJpaEntity userJpaEntity, FeedJpaEntity feedJpaEntity) {
        return SavedFeedJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .feedJpaEntity(feedJpaEntity)
                .build();
    }

    public SavedFeed toDomainEntity(SavedFeedJpaEntity savedFeedJpaEntity) {
        return SavedFeed.builder()
                .id(savedFeedJpaEntity.getSavedId())
                .userId(savedFeedJpaEntity.getUserJpaEntity().getUserId())
                .feedId(savedFeedJpaEntity.getFeedJpaEntity().getPostId())
                .createdAt(savedFeedJpaEntity.getCreatedAt())
                .modifiedAt(savedFeedJpaEntity.getModifiedAt())
                .status(savedFeedJpaEntity.getStatus())
                .build();
    }
}
