package konkuk.thip.room.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.application.port.out.dto.RoomParticipantQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface RoomParticipantQueryMapper {

    List<RoomGetHomeJoinedListResponse.JoinedRoomInfo> toHomeJoinedRoomResponse(List<RoomParticipantQueryDto> dtos);

    @Mapping(target = "userPercentage", expression = "java(dto.userPercentage().intValue())")
    RoomGetHomeJoinedListResponse.JoinedRoomInfo  toJoinedRoomInfo(RoomParticipantQueryDto dto);
}
