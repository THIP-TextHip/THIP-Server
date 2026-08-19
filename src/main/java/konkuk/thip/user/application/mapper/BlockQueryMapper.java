package konkuk.thip.user.application.mapper;

import konkuk.thip.user.adapter.in.web.response.UserBlockedListResponse;
import konkuk.thip.user.application.port.out.dto.BlockedUserQueryDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BlockQueryMapper {

    UserBlockedListResponse.BlockedUserDto toBlockedUserDto(BlockedUserQueryDto dto);

    List<UserBlockedListResponse.BlockedUserDto> toBlockedUserDtoList(List<BlockedUserQueryDto> dtos);
}
