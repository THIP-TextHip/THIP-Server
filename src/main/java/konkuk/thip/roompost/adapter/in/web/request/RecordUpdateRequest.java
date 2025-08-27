package konkuk.thip.roompost.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import konkuk.thip.roompost.application.port.in.dto.record.RecordUpdateCommand;

public record RecordUpdateRequest(
        @Schema(description = "기록 내용", example = "띱은 최고의 서비스인가?")
        @NotBlank(message = "기록 내용은 필수입니다.")
        @Size(max = 500, message = "기록 내용은 최대 500자 입니다.")
        String content
) {
        public RecordUpdateCommand toCommand(Long userId, Long roomId, Long recordId) {
                return new RecordUpdateCommand(
                        roomId,
                        recordId,
                        userId,
                        this.content
                );
        }
}
