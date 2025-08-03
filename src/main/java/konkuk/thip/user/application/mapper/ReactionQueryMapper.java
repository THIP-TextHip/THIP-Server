package konkuk.thip.user.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.user.adapter.in.web.response.UserReactionResponse;
import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

import static konkuk.thip.common.post.PostType.*;

@Mapper(componentModel = "spring")
public interface ReactionQueryMapper {

    @Mapping(target = "feedId", source = ".", qualifiedByName = "mapFeedId")
    @Mapping(target = "postId", source = ".", qualifiedByName = "mapPostId")
    @Mapping(target = "writerId", source = "userId")
    @Mapping(target = "postDate", source = ".", qualifiedByName = "mapPostDate")
    UserReactionResponse.ReactionDto toReactionDto(ReactionQueryDto dto);

    List<UserReactionResponse.ReactionDto> toReactionDtoList(List<ReactionQueryDto> dtoList);

    @Named("mapFeedId")
    default Long mapFeedId(ReactionQueryDto dto) {
        return FEED.getType().equals(dto.type()) ? dto.id() : null;
    }

    @Named("mapPostId")
    default Long mapPostId(ReactionQueryDto dto) {
        return (VOTE.getType().equals(dto.type()) || RECORD.getType().equals(dto.type())) ? dto.id() : null;
    }

    @Named("mapPostDate")
    default String mapPostDate(ReactionQueryDto dto) {
        return DateUtil.formatBeforeTime(dto.createdAt());
    }
}