package konkuk.thip.roompost.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import konkuk.thip.roompost.application.port.in.dto.record.RecordCreateCommand;

@Schema(
    description = "기록 생성 요청 DTO"
)
public record RecordCreateRequest (
        @Schema(description = "기록을 생성할 책의 페이지 번호", example = "20")
        @NotNull(message = "page는 필수입니다.")
        Integer page,

        @Schema(description = "총평 여부", example = "true")
        @NotNull(message = "isOverview(= 총평 여부)는 필수입니다.")
        Boolean isOverview,

        @Schema(description = "기록 내용", example = "띱은 최고의 서비스인 것 같습니다.")
        @NotBlank(message = "기록 내용은 필수입니다.")
        @Size(max = 500, message = "기록 내용은 최대 500자 입니다.")
        String content
) {
    public RecordCreateCommand toCommand(Long roomId, Long creatorId) {
        return new RecordCreateCommand(
                creatorId,
                roomId,
                page,
                isOverview,
                content
        );
    }
}