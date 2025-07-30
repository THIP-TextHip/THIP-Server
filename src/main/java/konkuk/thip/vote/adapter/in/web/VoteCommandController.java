package konkuk.thip.vote.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.vote.adapter.in.web.request.VoteCreateRequest;
import konkuk.thip.vote.adapter.in.web.response.VoteCreateResponse;
import konkuk.thip.vote.application.port.in.VoteCreateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vote Command API", description = "투표 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class VoteCommandController {

    private final VoteCreateUseCase voteCreateUseCase;

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
}
