package konkuk.thip.room.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomGetDeadlinePopularResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
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
    RoomGetDeadlinePopularResponse.RoomGetDeadlinePopularDto toDeadlinePopularRoomDto(RoomQueryDto dto);

    List<RoomGetDeadlinePopularResponse.RoomGetDeadlinePopularDto> toDeadlinePopularRoomDtoList(List<RoomQueryDto> roomQueryDtos);



    @Mapping(target = "deadlineDate", expression = "java(DateUtil.formatAfterTime(dto.endDate()))")
    @Mapping(target = "isPublic", expression = "java(Boolean.TRUE.equals(dto.isPublic()))")
    RoomSearchResponse.RoomSearchDto toRoomSearchDto(RoomQueryDto dto);

    List<RoomSearchResponse.RoomSearchDto> toRoomSearchResponse(List<RoomQueryDto> dtos);
}
