package konkuk.thip.book.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "책 저장 상태 변경 요청 DTO")
public record PostBookIsSavedRequest(
        @Schema(description = "저장 여부 type (true -> 저장, false -> 저장 취소)", example = "true")
        @NotNull(message = "type은 필수입니다.")
        boolean type
) {
}
