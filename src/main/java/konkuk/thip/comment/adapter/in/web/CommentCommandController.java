package konkuk.thip.comment.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.comment.adapter.in.web.request.CommentCreateRequest;
import konkuk.thip.comment.adapter.in.web.request.CommentIsLikeRequest;
import konkuk.thip.comment.adapter.in.web.response.CommentIdResponse;
import konkuk.thip.comment.adapter.in.web.response.CommentIsLikeResponse;
import konkuk.thip.comment.application.port.in.CommentCreateUseCase;
import konkuk.thip.comment.application.port.in.CommentLikeUseCase;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentCommandController {

    private final CommentCreateUseCase commentCreateUseCase;
    private final CommentLikeUseCase commentLikeUseCase;

    /**
     * 댓글/답글 작성
     * parentId:{Long},isReplyRequest:true 답글
     * parentId:null,isReplyRequest:false 댓글
     */
    @PostMapping("/comments/{postId}")
    public BaseResponse<CommentIdResponse> createComment(@RequestBody @Valid final CommentCreateRequest request,
                                                         @PathVariable("postId") final Long postId,
                                                         @UserId final Long userId) {
        return BaseResponse.ok(CommentIdResponse.of(commentCreateUseCase.createComment(request.toCommand(userId,postId))));
    }

    //댓글 좋아요 상태 변경: true -> 좋아요, false -> 좋아요 취소
    @PostMapping("/comments/{commentId}/likes")
    public BaseResponse<CommentIsLikeResponse> likeComment(@RequestBody @Valid final CommentIsLikeRequest request,
                                                           @PathVariable("commentId") final Long commentId,
                                                           @UserId final Long userId) {
        return BaseResponse.ok(CommentIsLikeResponse.of(commentLikeUseCase.changeLikeStatusComment(request.toCommand(userId, commentId))));
    }

}
