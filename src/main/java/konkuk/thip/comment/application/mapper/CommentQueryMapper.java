package konkuk.thip.comment.application.mapper;

import konkuk.thip.comment.adapter.in.web.response.CommentForSinglePostResponse;
import konkuk.thip.comment.application.port.out.dto.CommentQueryDto;
import konkuk.thip.common.util.DateUtil;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        imports = DateUtil.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE       // 명시적으로 매핑하지 않은 필드를 무시하도록 설정
)
public interface CommentQueryMapper {

    /**
     * 정상(root) 댓글 매핑 (답글 제외)
     */
    @Mapping(target = "replyList", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "isLike", expression = "java(likedCommentIds.contains(root.commentId()))")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(root.createdAt()))")
    @Mapping(target = "aliasName", source = "root.alias")
    CommentForSinglePostResponse.RootCommentDto toRoot(CommentQueryDto root, @Context Set<Long> likedCommentIds);

    /**
     * 개별 답글 매핑
     */
    @Mapping(target = "isLike", expression = "java(likedCommentIds.contains(child.commentId()))")
    @Mapping(target = "postDate", expression = "java(DateUtil.formatBeforeTime(child.createdAt()))")
    @Mapping(target = "aliasName", source = "child.alias")
    CommentForSinglePostResponse.RootCommentDto.ReplyDto toReply(CommentQueryDto child, @Context Set<Long> likedCommentIds);

    /**
     * 답글 리스트 헬퍼
     */
    default List<CommentForSinglePostResponse.RootCommentDto.ReplyDto> mapReplies(List<CommentQueryDto> children, @Context Set<Long> likedCommentIds) {
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }
        return children.stream()
                .map(child -> toReply(child, likedCommentIds))
                .toList();
    }

    default CommentForSinglePostResponse.RootCommentDto toRootCommentResponseWithChildren(CommentQueryDto root, List<CommentQueryDto> children, @Context Set<Long> likedCommentIds) {
        List<CommentForSinglePostResponse.RootCommentDto.ReplyDto> replyDtos = mapReplies(children, likedCommentIds);

        if (root.isDeleted()) {     // 삭제된 루트 & children 이 존재하는 경우
            return CommentForSinglePostResponse.RootCommentDto.createDeletedRootCommentDto(replyDtos);
        }

        CommentForSinglePostResponse.RootCommentDto rootDto = toRoot(root, likedCommentIds);
        rootDto.replyList().addAll(replyDtos);
        return rootDto;
    }
}
