package konkuk.thip.room.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.room.adapter.in.web.request.RoomCreateRequest;
import konkuk.thip.room.adapter.in.web.request.RoomJoinRequest;
import konkuk.thip.room.adapter.in.web.response.RoomCreateResponse;
import konkuk.thip.room.application.port.in.RoomCreateUseCase;
import konkuk.thip.room.application.port.in.RoomJoinUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCommandController {

    private final RoomCreateUseCase roomCreateUseCase;
    private final RoomJoinUsecase roomJoinUsecase;

    /**
     * 방 생성 요청
     */
    @PostMapping("/rooms")
    public BaseResponse<RoomCreateResponse> createRoom(@Valid @RequestBody RoomCreateRequest request, @UserId Long userId) {
        return BaseResponse.ok(RoomCreateResponse.of(
                roomCreateUseCase.createRoom(request.toCommand(), userId)
        ));
    }

    /**
     * 방 참여하기/취소하기 요청
     */
    @PostMapping("/rooms/{roomId}/join")
    public BaseResponse joinRoom(@Valid @RequestBody final RoomJoinRequest request,
                                 @UserId final Long userId,
                                 @PathVariable final Long roomId) {

        roomJoinUsecase.changeJoinState(request.toCommand(userId, roomId));
        return BaseResponse.ok(null);
    }
}
