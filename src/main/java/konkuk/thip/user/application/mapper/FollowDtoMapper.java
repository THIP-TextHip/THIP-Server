package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.application.port.out.dto.FollowQueryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowDtoMapper {

    UserFollowersResponse.Follower toFollowerList(FollowQueryDto dto);

    UserFollowingResponse.Following toFollowingList(FollowQueryDto dto);
}