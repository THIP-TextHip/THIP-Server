package konkuk.thip.room.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomGetDeadlinePopularResponse;
import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.application.port.in.dto.MyRoomType;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface RoomQueryMapper {

    @Mapping(target = "endDate", expression = "java(DateUtil.formatAfterTime(dto.endDate()))")
    @Mapping(target = "type", expression = "java(myRoomType.getType())")
    RoomShowMineResponse.MyRoom toShowMyRoomResponse(RoomQueryDto dto, @Context MyRoomType myRoomType);

    @Mapping(
            target = "deadlineDate",
            expression = "java(DateUtil.formatAfterTime(dto.endDate()))"
    )
    RoomGetDeadlinePopularResponse.RoomDto toDeadlinePopularRoomDto(RoomQueryDto dto);

    List<RoomGetDeadlinePopularResponse.RoomDto> toDeadlinePopularRoomDtoList(List<RoomQueryDto> roomQueryDtos);
}
