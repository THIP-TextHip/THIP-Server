package konkuk.thip.record.application.mapper;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", imports = DateUtil.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordQueryMapper {

    @Mapping(target = "postId",         source = "dto.postId")
    @Mapping(target = "postDate",       expression = "java(DateUtil.formatBeforeTime(dto.postDate()))")
    @Mapping(target = "postType",       source = "dto.postType")
    @Mapping(target = "page",           source = "dto.page")
    @Mapping(target = "userId",         source = "dto.userId")
    @Mapping(target = "nickName",       source = "dto.nickName")
    @Mapping(target = "profileImageUrl",source = "dto.profileImageUrl")
    @Mapping(target = "content",        source = "content")
    @Mapping(target = "likeCount",      source = "dto.likeCount")
    @Mapping(target = "commentCount",   source = "dto.commentCount")
    @Mapping(target = "isOverview",     source = "dto.isOverview")
    @Mapping(target = "isLiked",        source = "isLiked")
    @Mapping(target = "isWriter",       source = "isWriter")
    @Mapping(target = "isLocked",       source = "isLocked")
    @Mapping(target = "voteItems",      source = "voteItems")
    RecordSearchResponse.PostDto toPostDto(
            PostQueryDto dto,
            String content,
            boolean isLiked,
            boolean isWriter,
            boolean isLocked,
            List<RecordSearchResponse.PostDto.VoteItemDto> voteItems
    );
}