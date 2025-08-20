package konkuk.thip.user.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.user.application.port.in.*;
import konkuk.thip.user.application.port.in.dto.UserReactionType;
import konkuk.thip.user.adapter.in.web.response.*;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.user.adapter.in.web.request.UserVerifyNicknameRequest;
import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.adapter.in.web.response.UserVerifyNicknameResponse;
import konkuk.thip.user.adapter.in.web.response.UserIsFollowingResponse;
import konkuk.thip.user.adapter.in.web.response.UserViewAliasChoiceResponse;
import konkuk.thip.user.application.port.in.dto.UserSearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.GET_USER_FOLLOW;
import static konkuk.thip.common.swagger.SwaggerResponseDescription.USER_SEARCH;

@Tag(name = "User Query API", description = "사용자가 주체가 되는 조회")
@Validated
@RestController
@RequiredArgsConstructor
public class UserQueryController {

    private final UserViewAliasChoiceUseCase userViewAliasChoiceUseCase;
    private final UserGetFollowUsecase userGetFollowUsecase;
    private final UserIsFollowingUsecase userIsFollowingUsecase;
    private final UserVerifyNicknameUseCase userVerifyNicknameUseCase;
    private final UserSearchUsecase userSearchUsecase;
    private final UserMyPageUseCase userMyPageUseCase;
    private final UserShowFollowingsInFeedViewUseCase userShowFollowingsInFeedViewUseCase;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "사용자가 입력한 닉네임이 중복되는지 확인합니다."
    )
    @PostMapping("/users/nickname")
    public BaseResponse<UserVerifyNicknameResponse> verifyNickname(@Valid @RequestBody final UserVerifyNicknameRequest request) {
        return BaseResponse.ok(UserVerifyNicknameResponse.of(
                userVerifyNicknameUseCase.isNicknameUnique(request.nickname()))
        );
    }

    @Operation(
            summary = "사용자 별칭 선택 화면 조회",
            description = "사용자가 별칭을 선택할 수 있는 화면을 조회합니다."
    )
    @GetMapping("/users/alias")
    public BaseResponse<UserViewAliasChoiceResponse> showAliasChoiceView() {
        return BaseResponse.ok(UserViewAliasChoiceResponse.of(
                userViewAliasChoiceUseCase.getAllAliasesAndCategories()
        ));
    }

    @Operation(
            summary = "사용자 팔로워 조회",
            description = "특정 사용자의 팔로워 목록을 조회합니다."
    )
    @ExceptionDescription(GET_USER_FOLLOW)
    @GetMapping("/users/{userId}/followers")
    public BaseResponse<UserFollowersResponse> showFollowers(
            @Parameter(description = "조회할 사용자 ID") @PathVariable final Long userId,
            @Parameter(hidden = true) @UserId final Long loginUserId,
            @Parameter(description = "커서") @RequestParam(required = false) final String cursor,
            @Parameter(description = "단일 요청 페이지 크기 (1~10)")
            @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size) {
        return BaseResponse.ok(userGetFollowUsecase.getUserFollowers(loginUserId,userId, cursor, size));
    }

    @Operation(
            summary = "내 팔로잉 조회",
            description = "내가 팔로우하는 사용자 목록을 조회합니다."
    )
    @ExceptionDescription(GET_USER_FOLLOW)
    @GetMapping("/users/my-followings")
    public BaseResponse<UserFollowingResponse> showMyFollowing(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "커서") @RequestParam(required = false) final String cursor,
            @Parameter(description = "단일 요청 페이지 크기 (1~10)")
            @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size) {
        return BaseResponse.ok(userGetFollowUsecase.getMyFollowing(userId, cursor, size));
    }

    @Deprecated
    @Operation(
            summary = "팔로잉 여부 조회",
            description = "특정 사용자가 다른 사용자를 팔로우하고 있는지 확인합니다."
    )
    @GetMapping("/users/{targetUserId}/is-following")
    public BaseResponse<UserIsFollowingResponse> checkIsFollowing(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "팔로우 여부를 확인할 대상 사용자 ID") @PathVariable final Long targetUserId) {
        return BaseResponse.ok(UserIsFollowingResponse.of(userIsFollowingUsecase.isFollowing(userId, targetUserId)));
    }


    @Operation(
            summary = "사용자 검색",
            description = "닉네임을 기준으로 사용자를 검색합니다. 정확도순 정렬을 지원합니다."
    )
    @ExceptionDescription(USER_SEARCH)
    @GetMapping("/users")
    public BaseResponse<UserSearchResponse> showSearchUsers(
            @Parameter(description = "검색어", example = "thip") @RequestParam @NotBlank(message = "검색어는 필수입니다.") final String keyword,
            @Parameter(description = "사용자가 검색어 입력을 '확정'했는지 여부 (입력 중: false, 입력 확정: true)", example = "false") @RequestParam(name = "isFinalized") final boolean isFinalized,
            @Parameter(description = "단일 검색 결과 페이지 크기 (1~30) / default : 30", example = "30") @RequestParam(required = false, defaultValue = "30") @Min(1) @Max(30) final Integer size,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(userSearchUsecase.searchUsers(UserSearchQuery.of(keyword, userId, size, isFinalized)));
    }

    @Operation(
            summary = "사용자 반응 조회",
            description = "사용자가 남긴 반응(좋아요, 댓글)을 조회합니다."
    )
    @GetMapping("/users/reactions")
    public BaseResponse<UserReactionResponse> showUserReaction(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "반응 타입 (LIKE, COMMENT) / default : 둘다", example = "LIKE")
            @RequestParam(required = false, defaultValue = "BOTH") final String type,
            @Parameter(description = "단일 요청 페이지 크기 (1~10) / default : 10", example = "10")
            @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(required = false) final String cursor) {
        return BaseResponse.ok(userMyPageUseCase.getUserReaction(userId, UserReactionType.from(type), size, cursor));
    }

    @Operation(
            summary = "사용자 유저 정보 조회 (마이페이지)",
            description = "사용자의 마이페이지 정보를 조회합니다."
    )
    @GetMapping("/users/my-page")
    public BaseResponse<UserProfileResponse> showUserMyPage(
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(userMyPageUseCase.getUserProfile(userId));
    }

    @Operation(
            summary = "사용자 유저 아이디 조회",
            description = "접속한 사용자의 유저 아이디를 조회합니다."
    )
    @GetMapping("/users/my-id")
    public BaseResponse<Long> showUserMyId(
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(userId);
    }

    @Operation(
            summary = "전체 피드 화면 상단의 내 팔로잉 리스트(= 내 띱 리스트) 조회",
            description = "내가 팔로잉 하는 사람들을 반환합니다. 최근에 공개 피드를 작성한 사람들의 정보를 우선적으로 반환합니다. 최대 10명 반환합니다."
    )
    @GetMapping("/users/my-followings/recent-feeds")
    public BaseResponse<UserShowFollowingsInFeedViewResponse> showMyFollowingsInFeedView(
            @Parameter(hidden = true) @UserId Long userId
    ) {
        return BaseResponse.ok(userShowFollowingsInFeedViewUseCase.showMyFollowingsInFeedView(userId));
    }
}
