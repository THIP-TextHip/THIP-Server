package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FollowQueryMapper {

    // UserQueryDto의 userId와 Context로 넘어온 userId를 비교해서 true,false를 isMyself 필드에 주입
    @Mapping(target = "isMyself", source = "dto.userId", qualifiedByName = "isMyself")
    UserFollowersResponse.FollowerDto toFollowerDto(UserQueryDto dto, @Context Long loginUserId);

    @Named("isMyself")
    default boolean isMyself(Long userId, @Context Long loginUserId) {
        return userId != null && userId.equals(loginUserId);
    }

    @Mapping(target = "isFollowing", constant = "true")
    UserFollowingResponse.FollowingDto toFollowingDto(UserQueryDto dto);
}