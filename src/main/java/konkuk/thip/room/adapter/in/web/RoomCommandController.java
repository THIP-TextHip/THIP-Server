package konkuk.thip.room.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.room.adapter.in.web.request.RoomCreateRequest;
import konkuk.thip.room.adapter.in.web.request.RoomJoinRequest;
import konkuk.thip.room.adapter.in.web.response.RoomCreateResponse;
import konkuk.thip.room.application.port.in.RoomCreateUseCase;
import konkuk.thip.room.application.port.in.RoomJoinUseCase;
import konkuk.thip.room.application.port.in.RoomRecruitCloseUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Room Command API", description = "방 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class RoomCommandController {

    private final RoomCreateUseCase roomCreateUseCase;
    private final RoomJoinUseCase roomJoinUsecase;
    private final RoomRecruitCloseUsecase roomRecruitCloseUsecase;

    /**
     * 방 생성 요청
     */
    @Operation(
            summary = "방 생성",
            description = "사용자가 방을 생성합니다. 방 생성 시 필요한 정보를 포함한 요청을 받습니다."
    )
    @ExceptionDescription(ROOM_CREATE)
    @PostMapping("/rooms")
    public BaseResponse<RoomCreateResponse> createRoom(
            @Valid @RequestBody RoomCreateRequest request,
            @Parameter(hidden = true) @UserId Long userId
    ) {
        return BaseResponse.ok(RoomCreateResponse.of(
                roomCreateUseCase.createRoom(request.toCommand(), userId)
        ));
    }

    /**
     * 방 참여하기/취소하기 요청
     */
    @Operation(
            summary = "방 참여 상태 변경",
            description = "사용자가 방에 참여하거나 참여를 취소합니다. join -> 방 참여, cancel -> 방 참여 취소"
    )
    @ExceptionDescription(ROOM_JOIN_CANCEL)
    @PostMapping("/rooms/{roomId}/join")
    public BaseResponse<Void> joinRoom(
            @Valid @RequestBody final RoomJoinRequest request,
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "참여/취소하려는 방의 ID", example = "1") @PathVariable final Long roomId
    ) {
        roomJoinUsecase.changeJoinState(request.toCommand(userId, roomId));
        return BaseResponse.ok(null);
    }

    /**
     * 방 모집 마감하기 요청
     */
    @Operation(
            summary = "방 모집 마감",
            description = "방장이 방의 모집을 마감합니다. 방장이 방 모집을 마감할 때 사용합니다."
    )
    @ExceptionDescription(ROOM_RECRUIT_CLOSE)
    @PostMapping("/rooms/{roomId}/close")
    public BaseResponse<Void> closeRoomRecruit(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "모집을 마감할 방의 ID", example = "1") @PathVariable final Long roomId) {
        roomRecruitCloseUsecase.closeRoomRecruit(userId, roomId);
        return BaseResponse.ok(null);
    }
}
