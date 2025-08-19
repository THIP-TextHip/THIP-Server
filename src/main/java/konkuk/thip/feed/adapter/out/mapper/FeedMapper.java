package konkuk.thip.feed.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.feed.domain.TagList;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedMapper {

    private final ContentMapper contentMapper;

    public FeedJpaEntity toJpaEntity(Feed feed, UserJpaEntity userJpaEntity, BookJpaEntity bookJpaEntity) {
        return FeedJpaEntity.builder()
                .content(feed.getContent())
                .userJpaEntity(userJpaEntity)
                .isPublic(feed.getIsPublic())
                .reportCount(feed.getReportCount())
                .likeCount(feed.getLikeCount())
                .commentCount(feed.getCommentCount())
                .bookJpaEntity(bookJpaEntity)
                .contentList(new ArrayList<>())
                .tagList(feed.getTagList())
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
                .contentList(feedJpaEntity.getContentList().stream()
                                .map(contentMapper::toDomainEntity)
                                .toList())
                .createdAt(feedJpaEntity.getCreatedAt())
                .modifiedAt(feedJpaEntity.getModifiedAt())
                .status(feedJpaEntity.getStatus())
                .build();
    }
}
