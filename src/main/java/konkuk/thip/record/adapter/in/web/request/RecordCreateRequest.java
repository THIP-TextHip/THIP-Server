package konkuk.thip.record.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;

public record RecordCreateRequest (
    @NotNull(message = "page는 필수입니다.")
    Integer page,

    @NotNull(message = "isOverview(= 총평 여부)는 필수입니다.")
    Boolean isOverview,

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