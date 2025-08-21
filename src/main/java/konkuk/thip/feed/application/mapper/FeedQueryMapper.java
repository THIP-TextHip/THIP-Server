package konkuk.thip.feed.application.mapper;

import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.*;
import konkuk.thip.feed.application.port.in.dto.TagsWithCategoryResult;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.value.Tag;
import konkuk.thip.feed.domain.value.TagList;
import konkuk.thip.feed.domain.value.ContentList;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.domain.value.Alias;
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

    /**
     * 피드 전체 조회 응답 DTO 변환
     */
    @Mapping(target = "aliasName", source = "dto.alias")
    @Mapping(target = "aliasColor", expression = "java(Alias.from(dto.alias()).getColor())")
    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowAllResponse.FeedShowAllDto toFeedShowAllResponse(
            FeedQueryDto dto,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds,
            @Context Long userId
    );

    /**
     * 내 피드 조회 응답 DTO 변환
     */
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))")
    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowMineResponse.FeedShowMineDto toFeedShowMineResponse(FeedQueryDto dto,
                                                                Set<Long> savedFeedIds,
                                                                Set<Long> likedFeedIds,
                                                                @Context Long userId);

    /**
     * 특정 유저의 공개 피드 조회 응답 DTO 변환
     */
    @Mapping(target = "isSaved", expression = "java(savedFeedIds.contains(dto.feedId()))")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowByUserResponse.FeedShowByUserDto toFeedShowByUserResponse(
            FeedQueryDto dto,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds,
            @Context Long userId
    );

    /**
     * 특정 유저의 피드 조회 응답 DTO 변환
     */
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

    /**
     * 피드 상세 조회 응답 DTO 변환
     */
    @Mapping(target = "feedId", source = "feed.id")
    @Mapping(target = "creatorId", source = "feedCreator.id")
    @Mapping(target = "creatorNickname", source = "feedCreator.nickname")
    @Mapping(target = "creatorProfileImageUrl", source = "feedCreator.alias.imageUrl")
    @Mapping(target = "aliasName", source = "feedCreator.alias.value")
    @Mapping(target = "aliasColor", source = "feedCreator.alias.color")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(feed.getCreatedAt()))")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookImageUrl", source = "book.imageUrl")
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
    @Mapping(target = "isPublic", source = "feed.isPublic")
    FeedShowSingleResponse toFeedShowSingleResponse(Feed feed, User feedCreator, Book book, boolean isSaved, boolean isLiked, @Context Long userId);

    @Named("mapContentList")
    default String[] mapContentList(ContentList contentList) {
        if (contentList == null) return new String[0];
        return contentList.toArray(String[]::new);
    }

    @Named("mapTagList")
    default String[] mapTagList(TagList tagList) {
        if (tagList == null) return new String[0];
        return tagList.stream().map(Tag::getValue).toArray(String[]::new);
    }

    default List<TagsWithCategoryResult> toTagsWithCategoryResult(Map<Category, List<Tag>> categoryToTags) {
        Map<String, Set<String>> grouped = categoryToTags.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getKey().getValue(),
                        Collectors.flatMapping(
                                e -> e.getValue().stream().map(Tag::getValue),
                                Collectors.toSet()
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

    /**
     * 특정 책 관련 피드 조회 응답 DTO 변환
     */
    @Mapping(target = "feedId", source = "dto.feedId")
    @Mapping(target = "creatorId", source = "dto.creatorId")
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    @Mapping(target = "creatorNickname", source = "dto.creatorNickname")
    @Mapping(target = "creatorProfileImageUrl", source = "dto.creatorProfileImageUrl")
    @Mapping(target = "aliasName", expression = "java(Alias.from(dto.alias()).getValue())")
    @Mapping(target = "aliasColor", expression = "java(Alias.from(dto.alias()).getColor())")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))")
    @Mapping(target = "isbn", source = "dto.isbn")
    @Mapping(target = "bookTitle", source = "dto.bookTitle")
    @Mapping(target = "bookAuthor", source = "dto.bookAuthor")
    @Mapping(target = "contentBody", source = "dto.contentBody")
    @Mapping(target = "contentUrls", source = "dto.contentUrls")
    @Mapping(target = "likeCount", source = "dto.likeCount")
    @Mapping(target = "commentCount", source = "dto.commentCount")
    @Mapping(target = "isSaved", source = "isSaved")
    @Mapping(target = "isLiked", source = "isLiked")
    FeedRelatedWithBookResponse.FeedRelatedWithBookDto toFeedRelatedWithBookDto(
            FeedQueryDto dto,
            boolean isSaved,
            boolean isLiked,
            @Context Long userId
    );

    default List<FeedRelatedWithBookResponse.FeedRelatedWithBookDto> toFeedRelatedWithBookDtos(
            List<FeedQueryDto> dtos,
            Set<Long> savedFeedIds,
            Set<Long> likedFeedIds,
            @Context Long userId
    ) {
        return dtos.stream()
                .map(dto -> toFeedRelatedWithBookDto(dto, savedFeedIds.contains(dto.feedId()), likedFeedIds.contains(dto.feedId()), userId))
                .collect(Collectors.toList());
    }

    @Mapping(target = "aliasName", source = "dto.alias")
    @Mapping(target = "aliasColor", expression = "java(Alias.from(dto.alias()).getColor())")
    @Mapping(target = "isSaved",  constant = "true")
    @Mapping(target = "isLiked", expression = "java(likedFeedIds.contains(dto.feedId()))")
    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    FeedShowSavedListResponse.FeedShowSavedInfoDto toFeedShowSavedListResponse(FeedQueryDto dto, Set<Long> likedFeedIds, @Context Long userId);
}