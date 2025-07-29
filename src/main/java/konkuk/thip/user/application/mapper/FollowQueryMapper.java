package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowQueryMapper {

    UserFollowersResponse.Follower toFollowerList(UserQueryDto dto);

    UserFollowingResponse.Following toFollowingList(UserQueryDto dto);
}