package konkuk.thip.vote.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.vote.adapter.in.web.request.VoteCreateRequest;
import konkuk.thip.vote.adapter.in.web.request.VoteRequest;
import konkuk.thip.vote.adapter.in.web.response.VoteCreateResponse;
import konkuk.thip.vote.adapter.in.web.response.VoteResponse;
import konkuk.thip.vote.application.port.in.VoteCreateUseCase;
import konkuk.thip.vote.application.port.in.VoteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.VOTE;

@Tag(name = "Vote Command API", description = "투표 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class VoteCommandController {

    private final VoteCreateUseCase voteCreateUseCase;
    private final VoteUseCase voteUseCase;

    @Operation(
            summary = "투표 생성",
            description = "방에 대한 투표를 생성합니다."
    )
    @PostMapping("/rooms/{roomId}/vote")
    public BaseResponse<VoteCreateResponse> createVote(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(description = "투표를 생성할 방 ID", example = "1") @PathVariable Long roomId,
            @Valid @RequestBody VoteCreateRequest request) {

        return BaseResponse.ok(VoteCreateResponse.of(
                voteCreateUseCase.createVote(request.toCommand(userId, roomId))
        ));
    }

    @Operation(
            summary = "투표하기",
            description = "특정 투표에 대해 사용자가 투표를 진행합니다. type이 true이면 투표하기, false이면 투표 취소입니다."
    )
    @ExceptionDescription(VOTE)
    @PostMapping("/rooms/{roomId}/vote/{voteId}")
    public BaseResponse<VoteResponse> vote(
            @Parameter(hidden = true) @UserId Long userId,
            @Parameter(description = "투표를 진행할 방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "투표할 투표 ID", example = "1") @PathVariable Long voteId,
            @Valid @RequestBody VoteRequest request) {
        return BaseResponse.ok(VoteResponse.of(
                        voteUseCase.vote(request.toCommand(userId, roomId, voteId)))
        );
    }
}
