package konkuk.thip.roompost.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteUpdateCommand;

public record VoteUpdateRequest(
        @Schema(description = "투표 내용", example = "띱은 최고의 서비스인가?")
        @NotBlank(message = "투표 내용은 필수입니다.")
        @Size(max = 20, message = "투표 내용은 최대 20자 입니다.")
        String content
) {
        public VoteUpdateCommand toCommand(Long userId, Long roomId, Long voteId) {
                return new VoteUpdateCommand(
                        roomId,
                        voteId,
                        userId,
                        this.content
                );
        }
}
