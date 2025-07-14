package konkuk.thip.room.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetMemberListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.in.RoomGetHomeJoinedListUseCase;
import konkuk.thip.room.application.port.in.RoomGetMemberListUseCase;
import konkuk.thip.room.application.port.in.RoomSearchUseCase;
import jakarta.validation.Valid;
import konkuk.thip.room.adapter.in.web.request.RoomVerifyPasswordRequest;
import konkuk.thip.room.application.port.in.RoomShowRecruitingDetailViewUseCase;
import konkuk.thip.room.application.port.in.RoomVerifyPasswordUseCase;
import konkuk.thip.room.application.port.in.dto.RoomGetHomeJoinedListQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoomQueryController {

    private final RoomSearchUseCase roomSearchUseCase;
    private final RoomGetHomeJoinedListUseCase roomGetHomeJoinedListUseCase;
    private final RoomVerifyPasswordUseCase roomVerifyPasswordUseCase;
    private final RoomShowRecruitingDetailViewUseCase roomShowRecruitingDetailViewUseCase;
    private final RoomGetHomeJoinedListUseCase roomGetHomeJoinedListUseCase;
    private final RoomGetMemberListUseCase roomGetMemberListUseCase;

    @GetMapping("/rooms/search")
    public BaseResponse<RoomSearchResponse> searchRooms(
            @RequestParam(value = "keyword", required = false, defaultValue = "") final String keyword,
            @RequestParam(value = "category", required = false, defaultValue = "") final String category,
            @RequestParam("sort") final String sort,
            @RequestParam("page") final int page
    ) {
        return BaseResponse.ok(roomSearchUseCase.searchRoom(keyword, category, sort, page));
    }

    //비공개 방 비밀번호 입력 검증
    @PostMapping("/rooms/{roomId}/password")
    public BaseResponse<Void> verifyRoomPassword(@PathVariable("roomId") final Long roomId,
                                                 @Valid @RequestBody final RoomVerifyPasswordRequest roomVerifyPasswordRequest
                                                 ) {
        return BaseResponse.ok(roomVerifyPasswordUseCase.verifyRoomPassword(roomVerifyPasswordRequest.toQuery(roomId)));
    }

    @GetMapping("/rooms/{roomId}/recruiting")
    public BaseResponse<RoomRecruitingDetailViewResponse> getRecruitingRoomDetailView(
            @UserId final Long userId,
            @PathVariable("roomId") final Long roomId) {
        return BaseResponse.ok(roomShowRecruitingDetailViewUseCase.getRecruitingRoomDetailView(userId, roomId));
    }

    //[모임 홈] 참여중인 내 모임방 조회
    @GetMapping("/rooms/home/joined")
    public BaseResponse<RoomGetHomeJoinedListResponse> getHomeJoinedRooms(@UserId final Long userId,
                                                                         @RequestParam("page") final int page) {
        return BaseResponse.ok(roomGetHomeJoinedListUseCase.getHomeJoinedRoomList(
                RoomGetHomeJoinedListQuery.builder()
                        .userId(userId)
                        .page(page).build()));
    }

    // 독서메이트 조회
    @GetMapping("/rooms/{roomId}/users")
    public BaseResponse<RoomGetMemberListResponse> getRoomMemberList(@PathVariable("roomId") final Long roomId){
        return BaseResponse.ok(roomGetMemberListUseCase.getRoomMemberList(roomId));
    }

}
