package konkuk.thip.room.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import konkuk.thip.room.application.port.in.dto.RoomVerifyPasswordQuery;

public record RoomVerifyPasswordRequest(

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "\\d{4}", message = "비밀번호는 숫자 4자리여야 합니다.")
        String password
) {
    public RoomVerifyPasswordQuery toQuery(Long roomId) {
        return new RoomVerifyPasswordQuery(
                roomId,
                password);
    }
}
