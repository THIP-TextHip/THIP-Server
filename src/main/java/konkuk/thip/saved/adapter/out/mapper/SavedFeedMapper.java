package konkuk.thip.saved.adapter.out.mapper;

import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import konkuk.thip.saved.domain.SavedFeed;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
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
