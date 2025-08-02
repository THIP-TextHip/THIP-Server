package konkuk.thip.comment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.comment.adapter.in.web.request.CommentCreateRequest;
import konkuk.thip.comment.adapter.in.web.request.CommentIsLikeRequest;
import konkuk.thip.comment.adapter.in.web.response.CommentIdResponse;
import konkuk.thip.comment.adapter.in.web.response.CommentIsLikeResponse;
import konkuk.thip.comment.application.port.in.CommentCreateUseCase;
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

    private final CommentCreateUseCase commentCreateUseCase;
    private final CommentLikeUseCase commentLikeUseCase;
    private final CommentDeleteUseCase commentDeleteUseCase;

    /**
     * 댓글/답글 작성
     * parentId:{Long},isReplyRequest:true 답글
     * parentId:null,isReplyRequest:false 댓글
     */
    @Operation(
            summary = "댓글 작성",
            description = "사용자가 댓글을 작성합니다.\n" +
                    "답글 작성 시 parentId를 지정하고 isReplyRequest를 true로 설정합니다. " +
                    "댓글 작성 시 parentId는 null로 설정하고 isReplyRequest를 false로 설정합니다."
    )
    @ExceptionDescription(COMMENT_CREATE)
    @PostMapping("/comments/{postId}")
    public BaseResponse<CommentIdResponse> createComment(
            @RequestBody @Valid final CommentCreateRequest request,
            @Parameter(description = "댓글을 작성하려는 게시물 ID", example = "1") @PathVariable("postId") final Long postId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(CommentIdResponse.of(commentCreateUseCase.createComment(request.toCommand(userId,postId))));
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
    public BaseResponse<Void> deleteComment(
            @Parameter(description = "삭제하려는 댓글 ID", example = "1") @PathVariable("commentId") final Long commentId,
            @Parameter(hidden = true) @UserId final Long userId) {
        commentDeleteUseCase.deleteComment(commentId,userId);
        return BaseResponse.ok(null);
    }

}
