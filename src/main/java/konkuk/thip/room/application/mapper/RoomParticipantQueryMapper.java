package konkuk.thip.room.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.application.port.out.dto.RoomParticipantQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static konkuk.thip.room.domain.value.RoomStatus.IN_PROGRESS;
import static konkuk.thip.room.domain.value.RoomStatus.RECRUITING;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface RoomParticipantQueryMapper {

    List<RoomGetHomeJoinedListResponse.JoinedRoomInfo> toHomeJoinedRoomResponse(List<RoomParticipantQueryDto> dtos);

    default RoomGetHomeJoinedListResponse.JoinedRoomInfo toJoinedRoomInfo(RoomParticipantQueryDto dto) {
        int userPercentage = -1;
        String deadlineDate = null;

        if (IN_PROGRESS.equals(dto.roomStatus())) {
            userPercentage = dto.userPercentage().intValue();
        } else if (RECRUITING.equals(dto.roomStatus())) {
            deadlineDate = DateUtil.RecruitingRoomFormatAfterTimeSimple(dto.startDate());
        }

        return new RoomGetHomeJoinedListResponse.JoinedRoomInfo(
                dto.roomId(),
                dto.bookImageUrl(),
                dto.roomTitle(),
                dto.memberCount(),
                userPercentage,
                deadlineDate
        );
    }

}
