package konkuk.thip.room.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetMemberListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.in.*;
import konkuk.thip.room.application.port.in.RoomGetHomeJoinedListUseCase;
import konkuk.thip.room.application.port.in.RoomGetMemberListUseCase;
import konkuk.thip.room.application.port.in.RoomSearchUseCase;
import jakarta.validation.Valid;
import konkuk.thip.room.adapter.in.web.request.RoomVerifyPasswordRequest;
import konkuk.thip.room.application.port.in.dto.RoomGetHomeJoinedListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Room Query API", description = "방 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class RoomQueryController {

    private final RoomSearchUseCase roomSearchUseCase;
    private final RoomGetHomeJoinedListUseCase roomGetHomeJoinedListUseCase;
    private final RoomVerifyPasswordUseCase roomVerifyPasswordUseCase;
    private final RoomShowRecruitingDetailViewUseCase roomShowRecruitingDetailViewUseCase;
    private final RoomGetMemberListUseCase roomGetMemberListUseCase;
    private final RoomShowPlayingDetailViewUseCase roomShowPlayingDetailViewUseCase;

    @Operation(
            summary = "방 검색",
            description = "키워드, 카테고리, 정렬 방식, 페이지 번호를 기준으로 방을 검색합니다."
    )
    @ExceptionDescription(ROOM_SEARCH)
    @GetMapping("/rooms/search")
    public BaseResponse<RoomSearchResponse> searchRooms(
            @Parameter(description = "검색 키워드 (책 이름 or 방 이름", example = "해리") @RequestParam(value = "keyword", required = false, defaultValue = "") final String keyword,
            @Parameter(description = "모임방 카테고리", example = "문학") @RequestParam(value = "category", required = false, defaultValue = "") final String category,
            @Parameter(description = "정렬 방식 (마감 임박 : deadline, 신청 인원 : memberCount)", example = "deadline") @RequestParam("sort") final String sort,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam("page") final int page
    ) {
        return BaseResponse.ok(roomSearchUseCase.searchRoom(keyword, category, sort, page));
    }

    @Operation(
            summary = "비공개 방 비밀번호 입력 검증",
            description = "비공개 방에 참여하기 위해 비밀번호를 검증합니다."
    )
    @ExceptionDescription(ROOM_PASSWORD_CHECK)
    @PostMapping("/rooms/{roomId}/password")
    public BaseResponse<Void> verifyRoomPassword(
            @Parameter(description = "비밀번호 검증하려는 비공개 방 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Valid @RequestBody final RoomVerifyPasswordRequest roomVerifyPasswordRequest
    ) {
        return BaseResponse.ok(roomVerifyPasswordUseCase.verifyRoomPassword(roomVerifyPasswordRequest.toQuery(roomId)));
    }

    @Operation(
            summary = "모집중인 방 상세보기",
            description = "모집중인 방의 상세 정보를 조회합니다."
    )
    @ExceptionDescription(ROOM_RECRUITING_DETAIL_VIEW)
    @GetMapping("/rooms/{roomId}/recruiting")
    public BaseResponse<RoomRecruitingDetailViewResponse> getRecruitingRoomDetailView(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "상세보기 하려는 방의 ID", example = "1") @PathVariable("roomId") final Long roomId) {
        return BaseResponse.ok(roomShowRecruitingDetailViewUseCase.getRecruitingRoomDetailView(userId, roomId));
    }

    @Operation(
            summary = "[모임 홈] 참여중인 내 모임방 조회",
            description = "사용자가 참여중인 모임방 목록을 조회합니다."
    )
    @ExceptionDescription(ROOM_GET_HOME_JOINED_LIST)
    @GetMapping("/rooms/home/joined")
    public BaseResponse<RoomGetHomeJoinedListResponse> getHomeJoinedRooms(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam("page") final int page) {
        return BaseResponse.ok(roomGetHomeJoinedListUseCase.getHomeJoinedRoomList(
                RoomGetHomeJoinedListQuery.builder()
                        .userId(userId)
                        .page(page).build()));
    }

    @Operation(
            summary = "독서메이트(방 참여자) 조회",
            description = "특정 방의 참여자 목록을 조회합니다."
    )
    @ExceptionDescription(ROOM_GET_MEMBER_LIST)
    @GetMapping("/rooms/{roomId}/users")
    public BaseResponse<RoomGetMemberListResponse> getRoomMemberList(
            @Parameter(description = "방 참여자 목록을 조회하려는 방의 ID", example = "1") @PathVariable("roomId") final Long roomId){
        return BaseResponse.ok(roomGetMemberListUseCase.getRoomMemberList(roomId));
    }

    // 진행중인 방 상세보기
    @Operation(
            summary = "진행중인 방 상세보기",
            description = "진행중인 방의 상세 정보를 조회합니다."
    )
    @ExceptionDescription(ROOM_PLAYING_DETAIL)
    @GetMapping("/rooms/{roomId}/playing")
    public BaseResponse<RoomPlayingDetailViewResponse> getPlayingRoomDetailView(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "상세보기 하려는 방의 ID", example = "1") @PathVariable("roomId") final Long roomId
    ) {
        return BaseResponse.ok(roomShowPlayingDetailViewUseCase.getPlayingRoomDetailView(userId, roomId));
    }

}
