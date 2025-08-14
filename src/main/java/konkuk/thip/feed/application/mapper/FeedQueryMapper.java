package konkuk.thip.feed.application.mapper;

import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.*;
import konkuk.thip.feed.application.port.out.dto.TagCategoryQueryDto;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.domain.Content;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.user.domain.Alias;
import konkuk.thip.feed.application.port.in.dto.TagsWithCategoryResult;
import konkuk.thip.user.domain.User;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        imports = {DateUtil.class, Alias.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface FeedQueryMapper {

    @Mapping(target = "aliasName", source = "dto.alias")
    @Mapping(target = "aliasColor", expression = "java(Alias.from(dto.alias()).getColor())")
    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowAllResponse.FeedDto toFeedShowAllResponse(
            FeedQueryDto dto,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds,
            @Context Long userId
    );

    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))")
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowMineResponse.FeedDto toFeedShowMineDto(FeedQueryDto dto, @Context Long userId);

    List<FeedShowMineResponse.FeedDto> toFeedShowMineResponse(List<FeedQueryDto> dtos, @Context Long userId);

    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowByUserResponse.FeedDto toFeedShowByUserResponse(
            FeedQueryDto dto,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds,
            @Context Long userId
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
    @Mapping(target = "aliasName", source = "feedCreator.alias.value")
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
    @Mapping(target = "isWriter", source = "feedCreator.id", qualifiedByName = "isWriter")
    @Mapping(target = "tagList", source = "feed.tagList", qualifiedByName = "mapTagList")
    FeedShowSingleResponse toFeedShowSingleResponse(Feed feed, User feedCreator, Book book, boolean isSaved, boolean isLiked,
                                                    @Context Long userId);

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
        Map<String, Set<String>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        TagCategoryQueryDto::categoryValue,
                        Collectors.mapping(
                                TagCategoryQueryDto::tagValue,
                                Collectors.toCollection(LinkedHashSet::new)
                        )
                ));

        return grouped.entrySet().stream()
                .map(e -> new TagsWithCategoryResult(e.getKey(), new ArrayList<>(e.getValue())))
                .toList();
    }

    @Named("isWriter")
    default boolean isWriter(Long creatorId, @Context Long userId) {
        return creatorId != null && creatorId.equals(userId);
    }
}
