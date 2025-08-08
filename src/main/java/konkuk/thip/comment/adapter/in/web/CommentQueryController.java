package konkuk.thip.comment.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Comment Query API", description = "댓글 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class CommentQueryController {

    public final CommentShowAllUseCase commentShowAllUseCase;

    @Operation(
            summary = "댓글 전체 조회",
            description = "특정 게시글(= 피드, 기록, 투표) 의 댓글과 대댓글들을 전체 조회합니다."
    )
    @GetMapping("/comments/{postId}")
    public BaseResponse<CommentForSinglePostResponse> showAllCommentsOfPost(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "댓글을 조회할 게시글(= FEED, RECORD, VOTE)의 id값")
            @PathVariable("postId") final Long postId,
            @Parameter(description = "게시물 타입 (RECORD, VOTE, FEED)", example = "RECORD")
            @RequestParam(value = "postType") final String postType,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(commentShowAllUseCase.showAllCommentsOfPost(
                CommentShowAllQuery.of(postId, userId, postType, cursor)
        ));
    }
}
