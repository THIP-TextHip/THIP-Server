package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowQueryMapper {

    UserFollowersResponse.FollowerDto toFollowerDto(UserQueryDto dto);

    @Mapping(target = "isFollowing", constant = "true")
    UserFollowingResponse.FollowingDto toFollowingDto(UserQueryDto dto);
}