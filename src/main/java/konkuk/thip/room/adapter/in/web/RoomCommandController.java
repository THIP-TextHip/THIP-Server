package konkuk.thip.room.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.post.application.port.in.PostLikeUseCase;
import konkuk.thip.room.adapter.in.web.request.RoomCreateRequest;
import konkuk.thip.room.adapter.in.web.request.RoomJoinRequest;
import konkuk.thip.room.adapter.in.web.request.RoomPostIsLikeRequest;
import konkuk.thip.room.adapter.in.web.response.RoomCreateResponse;
import konkuk.thip.room.adapter.in.web.response.RoomJoinResponse;
import konkuk.thip.room.adapter.in.web.response.RoomPostIsLikeResponse;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitCloseResponse;
import konkuk.thip.room.application.port.in.RoomCreateUseCase;
import konkuk.thip.room.application.port.in.RoomJoinUseCase;
import konkuk.thip.room.application.port.in.RoomParticipantDeleteUseCase;
import konkuk.thip.room.application.port.in.RoomRecruitCloseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Room Command API", description = "방 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class RoomCommandController {

    private final RoomCreateUseCase roomCreateUseCase;
    private final RoomJoinUseCase roomJoinUsecase;
    private final RoomRecruitCloseUseCase roomRecruitCloseUsecase;
    private final RoomParticipantDeleteUseCase roomParticipantDeleteUseCase;
    private final PostLikeUseCase postLikeUseCase;

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
    public BaseResponse<RoomJoinResponse> joinRoom(
            @Valid @RequestBody final RoomJoinRequest request,
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "참여/취소하려는 방의 ID", example = "1") @PathVariable final Long roomId
    ) {
        return BaseResponse.ok(
                RoomJoinResponse.of(roomJoinUsecase.changeJoinState(request.toCommand(userId, roomId)))
        );
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
    public BaseResponse<RoomRecruitCloseResponse> closeRoomRecruit(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "모집을 마감할 방의 ID", example = "1") @PathVariable final Long roomId) {
        return BaseResponse.ok(
                RoomRecruitCloseResponse.of(roomRecruitCloseUsecase.closeRoomRecruit(userId, roomId))
        );
    }

    @Operation(
            summary = "방 게시물(기록,투표) 좋아요 상태 변경",
            description = "사용자가 방 게시물의 좋아요 상태를 변경합니다. (true -> 좋아요, false -> 좋아요 취소)"
    )
    @ExceptionDescription(CHANGE_ROOM_LIKE_STATE)
    @PostMapping("/room-posts/{postId}/likes")
    public BaseResponse<RoomPostIsLikeResponse> likeRoomPost(
            @RequestBody @Valid final RoomPostIsLikeRequest request,
            @Parameter(description = "좋아요 상태를 변경하려는 방 게시물 ID", example = "1")@PathVariable("postId") final Long postId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(RoomPostIsLikeResponse.of(postLikeUseCase.changeLikeStatusPost(request.toCommand(userId, postId))));
    }

    @Operation(
            summary = "방 나가기",
            description = "방장을 제외한 방의 멤버들이 방에서 나갑니다."
    )
    @ExceptionDescription(ROOM_LEAVE)
    @DeleteMapping("/rooms/{roomId}/leave")
    public BaseResponse<Void> deleteRoomParticipant(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "나갈 방의 ID", example = "1") @PathVariable final Long roomId) {
        return BaseResponse.ok(roomParticipantDeleteUseCase.leaveRoom(userId, roomId));
    }
}
