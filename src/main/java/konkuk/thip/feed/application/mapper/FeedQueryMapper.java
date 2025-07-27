package konkuk.thip.feed.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface FeedQueryMapper {

    @Mapping(
            target = "postDate",
            expression = "java(DateUtil.formatBeforeTime(dto.createdAt()))"
    )
    FeedShowAllResponse.FeedDto toFeedShowAllResponse(FeedQueryDto dto);

}
