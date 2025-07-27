package konkuk.thip.user.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.user.adapter.in.web.request.UserVerifyNicknameRequest;
import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.adapter.in.web.response.UserVerifyNicknameResponse;
import konkuk.thip.user.adapter.in.web.response.UserViewAliasChoiceResponse;
import konkuk.thip.user.application.port.in.UserGetFollowUsecase;
import konkuk.thip.user.application.port.in.UserVerifyNicknameUseCase;
import konkuk.thip.user.application.port.in.UserViewAliasChoiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.GET_USER_FOLLOW;

@Tag(name = "User Query API", description = "사용자가 주체가 되는 조회")
@RestController
@RequiredArgsConstructor
public class UserQueryController {

    private final UserViewAliasChoiceUseCase userViewAliasChoiceUseCase;
    private final UserGetFollowUsecase userGetFollowUsecase;
    private final UserVerifyNicknameUseCase userVerifyNicknameUseCase;

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
            @Parameter(description = "커서") @RequestParam(required = false) final String cursor,
            @Parameter(description = "단일 요청 페이지 크기 (1~10)")
            @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size) {
        return BaseResponse.ok(userGetFollowUsecase.getUserFollowers(userId, cursor, size));
    }

    @Operation(
            summary = "내 팔로잉 조회",
            description = "내가 팔로우하는 사용자 목록을 조회합니다."
    )
    @ExceptionDescription(GET_USER_FOLLOW)
    @GetMapping("/users/my/following")
    public BaseResponse<UserFollowingResponse> showMyFollowing(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "커서") @RequestParam(required = false) final String cursor,
            @Parameter(description = "단일 요청 페이지 크기 (1~10)")
            @RequestParam(defaultValue = "10") @Max(value = 10) @Min(value = 1) final int size) {
        return BaseResponse.ok(userGetFollowUsecase.getMyFollowing(userId, cursor, size));
    }
}
