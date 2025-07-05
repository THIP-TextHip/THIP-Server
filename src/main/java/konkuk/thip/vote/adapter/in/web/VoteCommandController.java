package konkuk.thip.vote.adapter.in.web;

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

@RestController
@RequiredArgsConstructor
public class VoteCommandController {

    private final VoteCreateUseCase voteCreateUseCase;

    @PostMapping("/rooms/{roomId}/vote")
    public BaseResponse<VoteCreateResponse> createVote(
            @UserId Long userId,
            @PathVariable Long roomId,
            @Valid @RequestBody VoteCreateRequest request) {

        return BaseResponse.ok(VoteCreateResponse.of(
                voteCreateUseCase.createVote(request.toCommand(userId, roomId))
        ));
    }
}
