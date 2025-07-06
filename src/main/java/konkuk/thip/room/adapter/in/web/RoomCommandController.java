package konkuk.thip.room.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.room.adapter.in.web.request.RoomCreateRequest;
import konkuk.thip.room.adapter.in.web.response.RoomCreateResponse;
import konkuk.thip.room.application.port.in.RoomCreateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCommandController {

    private final RoomCreateUseCase roomCreateUseCase;

    @PostMapping("/rooms")
    public BaseResponse<RoomCreateResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return BaseResponse.ok(RoomCreateResponse.of(
                roomCreateUseCase.createRoom(request.toCommand())
        ));
    }
}
