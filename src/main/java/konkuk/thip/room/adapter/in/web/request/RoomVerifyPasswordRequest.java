package konkuk.thip.room.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import konkuk.thip.room.application.port.in.dto.RoomVerifyPasswordQuery;

@Schema(description = "비공개 방 비밀번호 입력 검증 요청 DTO")
public record RoomVerifyPasswordRequest(

        @Schema(description = "방 비밀번호 (숫자 4자리)", example = "1234")
        @Pattern(regexp = "\\d{4}", message = "비밀번호는 숫자 4자리여야 합니다.")
        String password
) {
    public RoomVerifyPasswordQuery toQuery(Long roomId) {
        return new RoomVerifyPasswordQuery(
                roomId,
                password);
    }
}
