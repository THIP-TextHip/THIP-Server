package konkuk.thip.vote.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import konkuk.thip.vote.application.service.dto.VoteCommand;

@Schema(
        description = "투표하기 요청 DTO 정보"
)
public record VoteRequest(
        @Schema(
                description = "투표하려는 투표 항목 ID",
                example = "1"
        )
        Long voteItemId,
        @Schema(
                description = "투표 유형 (true: 투표하기, false: 투표 취소하기)",
                example = "true"
        )
        Boolean type
) {
    public VoteCommand toCommand(Long userId, Long roomId, Long voteId) {
        return new VoteCommand(userId, roomId, voteId, voteItemId, type);
    }
}
