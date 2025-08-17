package konkuk.thip.user.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.Oauth2Id;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.user.adapter.in.web.request.UserFollowRequest;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.request.UserUpdateRequest;
import konkuk.thip.user.adapter.in.web.response.UserFollowResponse;
import konkuk.thip.user.adapter.in.web.response.UserSignupResponse;
import konkuk.thip.user.application.port.in.UserFollowUsecase;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.UserUpdateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "User Command API", description = "사용자가 주체가 되는 정보 수정")
@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserSignupUseCase userSignupUseCase;
    private final UserFollowUsecase userFollowUsecase;
    private final UserUpdateUseCase userUpdateUseCase;


    @Operation(
            summary = "사용자 회원가입",
            description = "사용자가 회원가입을 합니다. OAuth2 ID를 통해 사용자를 식별합니다."
    )
    //TODO isTokenRequired 라는 파라미터 추가해서 안드는 바로 토큰 발급, 웹은 토큰 발급 여기서 안받고 다른 api 호출해서 temp -> access token으로 변경
    @ExceptionDescription(USER_SIGNUP)
    @PostMapping("/users/signup")
    public BaseResponse<UserSignupResponse> signup(
            @Valid @RequestBody final UserSignupRequest request,
            @Parameter(hidden = true) @Oauth2Id final String oauth2Id
    ) {
        return BaseResponse.ok(
                UserSignupResponse.of(userSignupUseCase.signup(request.toCommand(oauth2Id)))
        );
    }

    @Operation(
            summary = "사용자 팔로우 상태 변경",
            description = "특정 사용자를 팔로우하거나 언팔로우합니다. true 이면 팔로우, false 이면 언팔로우입니다."
    )
    @ExceptionDescription(CHANGE_FOLLOW_STATE)
    @PostMapping("/users/following/{followingUserId}")
    public BaseResponse<UserFollowResponse> followUser(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "팔로우/언팔로우할 사용자 ID") @PathVariable final Long followingUserId,
            @RequestBody @Valid final UserFollowRequest userFollowRequest) {
        return BaseResponse.ok(UserFollowResponse.of(userFollowUsecase.changeFollowingState(
                userFollowRequest.toCommand(userId, followingUserId)
        )));
    }

    @Operation(
            summary = "사용자 정보 수정",
            description = "사용자가 자신의 정보를 수정합니다. 닉네임과 칭호(Alias)를 수정할 수 있습니다."
    )
    @ExceptionDescription(USER_UPDATE)
    @PatchMapping("/users")
    public BaseResponse<Void> updateUser(
            @Parameter(hidden = true) @UserId final Long userId,
            @RequestBody @Valid final UserUpdateRequest userUpdateRequest) {
        userUpdateUseCase.updateUser(userUpdateRequest.toCommand(userId));
        return BaseResponse.ok(null);
    }
}
