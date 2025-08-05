package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserQueryMapper {

    // List<QueryDto> -> List<DTO>
    List<UserSearchResponse.UserDto> toUserDtoList(List<UserQueryDto> userQueryDtos);



}