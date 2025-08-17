package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.adapter.in.web.response.UserShowFollowingsInFeedViewResponse;
import konkuk.thip.user.application.port.out.dto.FollowingQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserQueryMapper {

    // List<QueryDto> -> List<DTO>
    List<UserSearchResponse.UserSearchDto> toUserDtoList(List<UserQueryDto> userQueryDtos);

    @Mapping(target = "userId", source = "dto.followingTargetUserId")
    @Mapping(target = "nickname", source = "dto.followingUserNickname")
    @Mapping(target = "profileImageUrl", source = "dto.followingUserProfileImageUrl")
    UserShowFollowingsInFeedViewResponse.UserShowFollowingsInFeedViewDto toFollowingFeedViewDto(FollowingQueryDto dto);

    List<UserShowFollowingsInFeedViewResponse.UserShowFollowingsInFeedViewDto> toFollowingFeedViewDtos(
            List<FollowingQueryDto> dtos
    );
}
