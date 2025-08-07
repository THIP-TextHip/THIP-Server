package konkuk.thip.comment.adapter.in.web;

import io.swagger.v3.oas.annotations.Parameter;
import konkuk.thip.comment.adapter.in.web.response.CommentForSinglePostResponse;
import konkuk.thip.comment.application.port.in.CommentShowAllUseCase;
import konkuk.thip.comment.application.port.in.dto.CommentShowAllQuery;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentQueryController {

    public final CommentShowAllUseCase commentShowAllUseCase;

    @GetMapping("/comments/{postId}")
    public BaseResponse<CommentForSinglePostResponse> showAllCommentsOfPost(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "댓글을 조회할 게시글(= FEED, RECORD, VOTE)의 id값")
            @PathVariable("postId") final Long postId,
            @Parameter(description = "게시물 타입 (RECORD, VOTE, FEED)", example = "RECORD")
            @RequestParam(value = "postType", required = false) final String postType,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(commentShowAllUseCase.showAllCommentsOfPost(
                CommentShowAllQuery.of(postId, userId, postType, cursor)
        ));
    }
}
