package konkuk.thip.feed.application.mapper;

import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.*;
import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.domain.Content;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.feed.application.port.in.dto.TagsWithCategoryResult;
import konkuk.thip.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface FeedQueryMapper {

    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    FeedShowAllResponse.FeedDto toFeedShowAllResponse(
            FeedQueryDto dto,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds
    );

    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))")
    FeedShowMineResponse.FeedDto toFeedShowMineDto(FeedQueryDto dto);

    List<FeedShowMineResponse.FeedDto> toFeedShowMineResponse(List<FeedQueryDto> dtos);

    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    FeedShowByUserResponse.FeedDto toFeedShowByUserResponse(
            FeedQueryDto dto,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds
    );

    @Mapping(target = "creatorId", source = "feedOwner.id")
    @Mapping(target = "profileImageUrl", source = "feedOwner.alias.imageUrl")
    @Mapping(target = "nickname", source = "feedOwner.nickname")
    @Mapping(target = "aliasName", source = "feedOwner.alias.value")
    @Mapping(target = "aliasColor", source = "feedOwner.alias.color")
    @Mapping(target = "followerCount", source = "feedOwner.followerCount")
    @Mapping(target = "totalFeedCount", source = "totalFeedCount")
    @Mapping(target = "isFollowing", source = "isFollowing")
    @Mapping(target = "latestFollowerProfileImageUrls", source = "latestFollowerProfileImageUrls")
    FeedShowUserInfoResponse toFeedShowUserInfoResponse(User feedOwner, int totalFeedCount, boolean isFollowing, List<String> latestFollowerProfileImageUrls);

    @Mapping(target = "feedId", source = "feed.id")
    @Mapping(target = "creatorId", source = "feedCreator.id")
    @Mapping(target = "creatorNickname", source = "feedCreator.nickname")
    @Mapping(target = "creatorProfileImageUrl", source = "feedCreator.alias.imageUrl")
    @Mapping(target = "alias", source = "feedCreator.alias.value")
    @Mapping(target = "aliasColor", source = "feedCreator.alias.color")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(feed.getCreatedAt()))")
    @Mapping(target = "isbn", source = "book.isbn")
    @Mapping(target = "bookAuthor", source = "book.authorName")
    @Mapping(target = "contentBody", source = "feed.content")
    @Mapping(target = "contentUrls", source = "feed.contentList", qualifiedByName = "mapContentList")
    @Mapping(target = "likeCount", source = "feed.likeCount")
    @Mapping(target = "commentCount", source = "feed.commentCount")
    @Mapping(target = "isSaved", source = "isSaved")
    @Mapping(target = "isLiked", source = "isLiked")
    @Mapping(target = "tagList", source = "feed.tagList", qualifiedByName = "mapTagList")
    FeedShowSingleResponse toFeedShowSingleResponse(Feed feed, User feedCreator, Book book, boolean isSaved, boolean isLiked);

    @Named("mapContentList")
    default String[] mapContentList(List<Content> contentList) {
        if (contentList == null) return new String[0];
        return contentList.stream().map(Content::getContentUrl).toArray(String[]::new);
    }

    @Named("mapTagList")
    default String[] mapTagList(List<Tag> tagList) {
        if (tagList == null) return new String[0];
        return tagList.stream().map(Tag::getValue).toArray(String[]::new);
    }

    default List<TagsWithCategoryResult> toTagsWithCategoryResult(List<TagCategoryQueryDto> rows) {
        Map<String, List<String>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        TagCategoryQueryDto::categoryValue,
                        Collectors.mapping(TagCategoryQueryDto::tagValue, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(e -> new TagsWithCategoryResult(e.getKey(), e.getValue()))
                .toList();
    }

}
