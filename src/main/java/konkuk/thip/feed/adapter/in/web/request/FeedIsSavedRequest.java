package konkuk.thip.feed.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedCommand;

@Schema(description = "피드 저장 상태 요청 DTO")
public record FeedIsSavedRequest(
        @Schema(description = "저장 상태 종류 (저장하기: true, 저장취소: false)", example = "true")
        @NotNull(message = "type은 필수입니다.")
        Boolean type
) {
    public static FeedIsSavedCommand toCommand(Long userId, Long feedId, Boolean type) {
        return new FeedIsSavedCommand(userId, feedId, type);
    }
}
