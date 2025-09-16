package konkuk.thip.notification.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.notification.adapter.in.web.response.NotificationShowResponse;
import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface NotificationQueryMapper {

    // 단건 매핑
    @Mapping(target = "notificationType",
            expression = "java(dto.notificationCategory().getDisplay())")
    @Mapping(target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))")
    NotificationShowResponse.NotificationOfUser toNotificationOfUser(NotificationQueryDto dto);

    // 컬렉션 매핑
    List<NotificationShowResponse.NotificationOfUser> toNotificationOfUsers(List<NotificationQueryDto> dtos);

}
