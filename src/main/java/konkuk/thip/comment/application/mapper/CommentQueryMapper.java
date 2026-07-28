package konkuk.thip.comment.application.mapper;

import konkuk.thip.comment.adapter.in.web.response.ChildCommentsResponse;
import konkuk.thip.comment.adapter.in.web.response.CommentCreateResponse;
import konkuk.thip.comment.adapter.in.web.response.RootCommentsResponse;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.DateUtil;
import org.mapstruct.*;

import java.util.Set;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface CommentQueryMapper {

    /**
     * 루트 댓글 조회 API용 매핑
     */
    @Mapping(target = "isLike", expression = "java(likedCommentIds.contains(root.commentId()))")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(root.createdAt()))")
    @Mapping(target = "aliasName", source = "root.alias")
    @Mapping(target = "isWriter", source = "root.creatorId", qualifiedByName = "isWriter")
    RootCommentsResponse.RootCommentDto toRootCommentResponse(CommentQueryDto root, @Context Set<Long> likedCommentIds, @Context Long userId);

    // 댓글/답글 생성시 루트 댓글 매핑
    @Mapping(target = "replyList", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "isLike", expression = "java(isLike)")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(root.createdAt()))")
    @Mapping(target = "aliasName", source = "root.alias")
    @Mapping(target = "isWriter", source = "root.creatorId", qualifiedByName = "isWriter")
    CommentCreateResponse toRoot(CommentQueryDto root, boolean isLike, @Context Long userId);

    // 답글 생성시 답글 매핑
    @Mapping(target = "isLike", constant = "false")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(child.createdAt()))")
    @Mapping(target = "aliasName", source = "child.alias")
    @Mapping(target = "isWriter", source = "child.creatorId", qualifiedByName = "isWriter")
    CommentCreateResponse.ReplyCommentCreateDto toReply(CommentQueryDto child, @Context Long userId);

    /**
     * 자식 댓글 조회 API용 매핑
     */
    @Mapping(target = "isLike", expression = "java(likedCommentIds.contains(child.commentId()))")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(child.createdAt()))")
    @Mapping(target = "aliasName", source = "child.alias")
    @Mapping(target = "isWriter", source = "child.creatorId", qualifiedByName = "isWriter")
    ChildCommentsResponse.ChildCommentDto toChildComment(CommentQueryDto child, @Context Set<Long> likedCommentIds, @Context Long userId);

    /**
     * 댓글 생성 시 루트 댓글과 답글을 함께 반환
     */
    default CommentCreateResponse toRootCommentResponseWithChildren(
            CommentQueryDto root, CommentQueryDto children, boolean isLikedParentComment, @Context Long userId) {
        CommentCreateResponse.ReplyCommentCreateDto replyDto = toReply(children, userId);

        CommentCreateResponse rootDto = toRoot(root, isLikedParentComment, userId);
        rootDto.replyList().add(replyDto);
        return rootDto;
    }


    @Named("isWriter")
    default boolean isWriter(Long creatorId, @Context Long userId) {
        return creatorId != null && creatorId.equals(userId);
    }
}
