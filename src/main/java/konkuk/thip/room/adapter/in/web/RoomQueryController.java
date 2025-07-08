package konkuk.thip.room.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.room.adapter.in.web.request.RoomVerifyPasswordRequest;
import konkuk.thip.room.application.port.in.RoomVerifyPasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomQueryController {

    private final RoomVerifyPasswordUseCase roomVerifyPasswordUseCase;

    //비공개 방 비밀번호 입력 검증
    @PostMapping("/rooms/{roomId}/password")
    public BaseResponse<Void> verifyRoomPassword(@UserId final Long userId,
                                                 @PathVariable("roomId") final Long roomId,
                                                 @Valid @RequestBody final RoomVerifyPasswordRequest roomVerifyPasswordRequest
                                                 ) {
        return BaseResponse.ok(roomVerifyPasswordUseCase.verifyRoomPassword(roomVerifyPasswordRequest.toQuery(userId,roomId)));
    }

}
