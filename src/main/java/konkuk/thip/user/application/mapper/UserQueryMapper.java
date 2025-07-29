package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserQueryMapper {

    UserSearchResponse.UserDto toUserDto(UserQueryDto dto);

}