package konkuk.thip.roompost.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.roompost.adapter.in.web.response.AttendanceCheckShowResponse;
import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface AttendanceCheckQueryMapper {

    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))")
    @Mapping(target = "date", expression = "java(dto.createdAt().toLocalDate())")
    @Mapping(target = "isWriter", source = "dto.creatorId", qualifiedByName = "isWriter")
    AttendanceCheckShowResponse.AttendanceCheckShowDto toAttendanceCheckShowDto(AttendanceCheckQueryDto dto, @Context Long userId);

    List<AttendanceCheckShowResponse.AttendanceCheckShowDto> toAttendanceCheckShowResponse(List<AttendanceCheckQueryDto> dtos, @Context Long userId);

    @Named("isWriter")
    default boolean isWriter(Long creatorId, @Context Long userId) {
        return creatorId != null && creatorId.equals(userId);
    }
}
