package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserFollowingRecentWritersResponse;
import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserQueryMapper {

    // List<QueryDto> -> List<DTO>
    List<UserSearchResponse.UserDto> toUserDtoList(List<UserQueryDto> userQueryDtos);

    // 단건 매핑: UserQueryDto -> RecentWriter
    UserFollowingRecentWritersResponse.RecentWriter toRecentWriter(UserQueryDto dto);
    // 리스트 매핑: List<UserQueryDto> -> List<RecentWriter>
    List<UserFollowingRecentWritersResponse.RecentWriter> toRecentWriterList(List<UserQueryDto> dtos);
    // 래핑: List<UserQueryDto> -> UserFollowingRecentWritersResponse
    default UserFollowingRecentWritersResponse toRecentWriterResponses(List<UserQueryDto> dtos) {
        return new  UserFollowingRecentWritersResponse(toRecentWriterList(dtos));
    }
}
