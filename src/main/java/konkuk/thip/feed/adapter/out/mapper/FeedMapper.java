package konkuk.thip.feed.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.value.TagList;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.feed.domain.value.ContentList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedMapper {

    public FeedJpaEntity toJpaEntity(Feed feed, UserJpaEntity userJpaEntity, BookJpaEntity bookJpaEntity) {
        return FeedJpaEntity.builder()
                .content(feed.getContent())
                .userJpaEntity(userJpaEntity)
                .isPublic(feed.getIsPublic())
                .reportCount(feed.getReportCount())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .bookJpaEntity(bookJpaEntity)
                .contentList(feed.getContentList() != null ? feed.getContentList() : ContentList.empty())
                .tagList(feed.getTagList() != null ? feed.getTagList() : TagList.empty())
                .build();
    }

    public Feed toDomainEntity(FeedJpaEntity feedJpaEntity) {
        return Feed.builder()
                .id(feedJpaEntity.getPostId())
                .content(feedJpaEntity.getContent())
                .creatorId(feedJpaEntity.getUserJpaEntity().getUserId())
                .isPublic(feedJpaEntity.getIsPublic())
                .reportCount(feedJpaEntity.getReportCount())
                .likeCount(feedJpaEntity.getLikeCount())
                .commentCount(feedJpaEntity.getCommentCount())
                .targetBookId(feedJpaEntity.getBookJpaEntity().getBookId())
                .tagList(feedJpaEntity.getTagList())
                .contentList(feedJpaEntity.getContentList() != null ? feedJpaEntity.getContentList() : ContentList.empty())
                .createdAt(feedJpaEntity.getCreatedAt())
                .modifiedAt(feedJpaEntity.getModifiedAt())
                .status(feedJpaEntity.getStatus())
                .build();
    }
}
