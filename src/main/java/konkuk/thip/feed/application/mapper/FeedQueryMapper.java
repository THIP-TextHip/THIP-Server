package konkuk.thip.feed.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedShowMineResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedShowUserInfoResponse;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

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

    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    FeedShowMineResponse.FeedDto toFeedShowMineResponse(FeedQueryDto dto);

    @Mapping(target = "profileImageUrl", source = "feedOwner.alias.imageUrl")
    @Mapping(target = "nickname", source = "feedOwner.nickname")
    @Mapping(target = "aliasName", source = "feedOwner.alias.value")
    @Mapping(target = "aliasColor", source = "feedOwner.alias.color")
    @Mapping(target = "followerCount", source = "feedOwner.followerCount")
    @Mapping(target = "totalFeedCount", source = "totalFeedCount")
    @Mapping(target = "latestFollowerProfileImageUrls", source = "latestFollowerProfileImageUrls")
    FeedShowUserInfoResponse toFeedShowUserInfoResponse(User feedOwner, int totalFeedCount, List<String> latestFollowerProfileImageUrls);
}
