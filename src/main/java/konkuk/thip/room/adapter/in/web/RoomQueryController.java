package konkuk.thip.room.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.room.adapter.in.web.request.RoomVerifyPasswordRequest;
import konkuk.thip.room.adapter.in.web.response.*;
import konkuk.thip.room.application.port.in.*;
import konkuk.thip.room.application.port.in.dto.MyRoomType;
import konkuk.thip.room.application.port.in.dto.RoomGetHomeJoinedListQuery;
import konkuk.thip.room.application.port.in.dto.RoomSearchQuery;
import lombok.RequiredArgsConstructor;
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
    private final RoomShowMineUseCase roomShowMineUseCase;
    private final RoomGetBookPageUseCase roomGetBookPageUseCase;
    private final RoomGetDeadlinePopularUseCase roomGetDeadlinePopularUsecase;

    @Operation(
            summary = "모집중인 방 검색",
            description = "검색어(= 방이름 or 책제목), 카테고리 와 매핑되는 모집중인 방을 검색합니다. 공개/비공개 방 모두 검색 가능합니다."
    )
    @ExceptionDescription(ROOM_SEARCH)
    @GetMapping("/rooms/search")
    public BaseResponse<RoomSearchResponse> searchRecruitingRooms(
            @Parameter(description = "검색 키워드 (책 이름 or 방 이름)", example = "해리") @RequestParam(value = "keyword", required = false, defaultValue = "") final String keyword,
            @Parameter(description = "모임방 카테고리", example = "문학") @RequestParam(value = "category", required = false, defaultValue = "") final String category,
            @Parameter(description = "정렬 방식 (마감 임박 : deadline, 신청 인원 : memberCount)", example = "deadline") @RequestParam("sort") final String sort,
            @Parameter(description = "사용자가 검색어 입력을 '확정'했는지 여부 (입력 중: false, 입력 확정: true)", example = "false") @RequestParam(name = "isFinalized") final boolean isFinalized,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(roomSearchUseCase.searchRecruitingRooms(
                RoomSearchQuery.of(keyword, category, sort, isFinalized, cursor, userId)
        ));
    }

    @Operation(
            summary = "비공개 방 비밀번호 입력 검증",
            description = "비공개 방에 참여하기 위해 비밀번호를 검증합니다."
    )
    @ExceptionDescription(ROOM_PASSWORD_CHECK)
    @PostMapping("/rooms/{roomId}/password")
    public BaseResponse<RoomVerifyPasswordResponse> verifyRoomPassword(
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
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(roomGetHomeJoinedListUseCase.getHomeJoinedRoomList(
                RoomGetHomeJoinedListQuery.builder()
                        .userId(userId)
                        .cursorStr(cursor).build()));
    }

    @Operation(
            summary = "독서메이트(방 참여자) 조회",
            description = "특정 방의 참여자 목록을 조회합니다."
    )
    @ExceptionDescription(ROOM_GET_MEMBER_LIST)
    @GetMapping("/rooms/{roomId}/users")
    public BaseResponse<RoomGetMemberListResponse> getRoomMemberList(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "방 참여자 목록을 조회하려는 방의 ID", example = "1") @PathVariable("roomId") final Long roomId){
        return BaseResponse.ok(roomGetMemberListUseCase.getRoomMemberList(userId, roomId));
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
            @PathVariable("roomId") final Long roomId
    ) {
        return BaseResponse.ok(roomShowPlayingDetailViewUseCase.getPlayingRoomDetailView(userId, roomId));
    }

    // 내 모임방 리스트 조회
    @Operation(
            summary = "내 모임방 리스트 조회",
            description = "사용자가 참여중인 모임방 목록을 조회합니다. 타입에 따라 모집중인 방과 진행중인 방을 구분할 수 있습니다."
    )
    @GetMapping("/rooms/my")
    public BaseResponse<RoomShowMineResponse> getMyRooms(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "조회할 방의 타입 (playingAndRecruiting: 진행중인 방과 모집중인 방, recruiting: 모집중인 방만, playing: 진행중인 방만, expired: 만료된 방만)", example = "playingAndRecruiting")
            @RequestParam(value = "type", required = false, defaultValue = "playingAndRecruiting") final String type,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(roomShowMineUseCase.getMyRooms(userId, MyRoomType.from(type), cursor));
    }

    @Operation(
            summary = "책 전체 페이지수 및 총평 가능 여부 조회 (in 기록 작성 화면)",
            description = "방 참여자가 책 기록을 남길 수 있는 최대 책 페이지 수와 총평 작성 가능 여부를 조회합니다."
    )
    @ExceptionDescription(ROOM_GET_BOOK_PAGE)
    @GetMapping("/rooms/{roomId}/book-page")
    public BaseResponse<RoomGetBookPageResponse> getBookPage(
            @Parameter(description = "방 ID", example = "1") @PathVariable("roomId") final Long roomId,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(roomGetBookPageUseCase.getBookPage(userId, roomId));
    }

    @Operation(
            summary = "마감 임박 및 인기 방 조회",
            description = "카테고리별로 마감 임박 방과 인기 방을 조회합니다."
    )
    @ExceptionDescription(ROOM_GET_DEADLINE_POPULAR)
    @GetMapping("/rooms")
    public BaseResponse<RoomGetDeadlinePopularResponse> getDeadlineAndPopularRoomList(
            @Parameter(description = "카테고리 이름 (default : 문학)", example = "과학/IT")
            @RequestParam(value = "category", defaultValue = "문학") final String category,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(roomGetDeadlinePopularUsecase.getDeadlineAndPopularRoomList(category, userId));
    }
}
