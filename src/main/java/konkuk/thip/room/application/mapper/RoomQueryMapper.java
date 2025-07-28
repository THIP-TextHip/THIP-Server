package konkuk.thip.room.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.application.port.out.dto.RoomShowMineQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface RoomQueryMapper {

    @Mapping(
            target = "endDate",
            expression = "java(DateUtil.formatAfterTime(dto.endDate()))"
    )
    RoomShowMineResponse.MyRoom toShowMyRoomResponse(RoomShowMineQueryDto dto);
}
