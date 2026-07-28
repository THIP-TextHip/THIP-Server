package konkuk.thip.comment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.comment.adapter.in.web.request.RootCommentCreateRequest;
import konkuk.thip.comment.adapter.in.web.request.ChildCommentCreateRequest;
import konkuk.thip.comment.adapter.in.web.request.CommentIsLikeRequest;
import konkuk.thip.comment.adapter.in.web.response.CommentDeleteResponse;
import konkuk.thip.comment.adapter.in.web.response.CommentCreateResponse;
import konkuk.thip.comment.adapter.in.web.response.CommentIsLikeResponse;
import konkuk.thip.comment.application.port.in.RootCommentCreateUseCase;
import konkuk.thip.comment.application.port.in.ChildCommentCreateUseCase;
import konkuk.thip.comment.application.port.in.CommentDeleteUseCase;
import konkuk.thip.comment.application.port.in.CommentLikeUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Comment Command API", description = "댓글 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class CommentCommandController {

    private final RootCommentCreateUseCase rootCommentCreateUseCase;
    private final ChildCommentCreateUseCase childCommentCreateUseCase;
    private final CommentLikeUseCase commentLikeUseCase;
    private final CommentDeleteUseCase commentDeleteUseCase;

    @Operation(
            summary = "루트 댓글 작성",
            description = "특정 게시글에 루트 댓글을 작성합니다."
    )
    @ExceptionDescription(COMMENT_CREATE)
    @PostMapping("/comments/{postId}")
    public BaseResponse<CommentCreateResponse> createRootComment(
            @RequestBody @Valid final RootCommentCreateRequest request,
            @Parameter(description = "댓글을 작성하려는 게시물 ID", example = "1") @PathVariable("postId") final Long postId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(rootCommentCreateUseCase.createRootComment(request.toCommand(userId, postId)));
    }

    @Operation(
            summary = "답글 작성",
            description = "특정 댓글에 답글(자식 댓글)을 작성합니다."
    )
    @ExceptionDescription(COMMENT_CREATE)
    @PostMapping("/comments/replies/{parentCommentId}")
    public BaseResponse<CommentCreateResponse> createChildComment(
            @RequestBody @Valid final ChildCommentCreateRequest request,
            @Parameter(description = "부모 댓글 ID", example = "1") @PathVariable("parentCommentId") final Long parentCommentId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(childCommentCreateUseCase.createChildComment(request.toCommand(userId, parentCommentId)));
    }

    @Operation(
            summary = "댓글 좋아요 상태 변경",
            description = "사용자가 댓글의 좋아요 상태를 변경합니다. (true -> 좋아요, false -> 좋아요 취소)"
    )
    @ExceptionDescription(CHANGE_COMMENT_LIKE_STATE)
    @PostMapping("/comments/{commentId}/likes")
    public BaseResponse<CommentIsLikeResponse> likeComment(
            @RequestBody @Valid final CommentIsLikeRequest request,
            @Parameter(description = "좋아요 상태를 변경하려는 댓글 ID", example = "1") @PathVariable("commentId") final Long commentId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(CommentIsLikeResponse.of(commentLikeUseCase.changeLikeStatusComment(request.toCommand(userId, commentId))));
    }

    @Operation(
            summary = "댓글 삭제",
            description = "사용자가 댓글을 삭제합니다."
    )
    @ExceptionDescription(COMMENT_DELETE)
    @DeleteMapping("/comments/{commentId}")
    public BaseResponse<CommentDeleteResponse> deleteComment(
            @Parameter(description = "삭제하려는 댓글 ID", example = "1") @PathVariable("commentId") final Long commentId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(CommentDeleteResponse.of(commentDeleteUseCase.deleteComment(commentId,userId)));
    }

}
