package konkuk.thip.user.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.Oauth2Id;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.user.adapter.in.web.request.UserFollowRequest;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.response.UserFollowResponse;
import konkuk.thip.user.adapter.in.web.response.UserSignupResponse;
import konkuk.thip.user.application.port.in.UserFollowUsecase;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.security.constant.AuthParameters.JWT_HEADER_KEY;
import static konkuk.thip.common.security.constant.AuthParameters.JWT_PREFIX;
import static konkuk.thip.common.swagger.SwaggerResponseDescription.CHANGE_FOLLOW_STATE;
import static konkuk.thip.common.swagger.SwaggerResponseDescription.USER_SIGNUP;

@Tag(name = "User Command API", description = "사용자가 주체가 되는 정보 수정")
@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserSignupUseCase userSignupUseCase;
    private final UserFollowUsecase userFollowUsecase;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "사용자 회원가입",
            description = "사용자가 회원가입을 합니다. OAuth2 ID를 통해 사용자를 식별합니다."
    )
    @ExceptionDescription(USER_SIGNUP)
    @PostMapping("/users/signup")
    public BaseResponse<UserSignupResponse> signup(@Valid @RequestBody final UserSignupRequest request,
                                                   @Parameter(hidden = true) @Oauth2Id final String oauth2Id,
                                                   HttpServletResponse response) {
        Long userId = userSignupUseCase.signup(request.toCommand(oauth2Id));
        String accessToken = jwtUtil.createAccessToken(userId);
        response.setHeader(JWT_HEADER_KEY.getValue(), JWT_PREFIX.getValue() + accessToken);
        return BaseResponse.ok(UserSignupResponse.of(userId));
    }


    /**
     * 사용자 팔로우 상태 변경 : true -> 팔로우, false -> 언팔로우
     */
    @Operation(
            summary = "사용자 팔로우 상태 변경",
            description = "특정 사용자를 팔로우하거나 언팔로우합니다."
    )
    @ExceptionDescription(CHANGE_FOLLOW_STATE)
    @PostMapping("/users/following/{followingUserId}")
    public BaseResponse<UserFollowResponse> followUser(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "팔로우/언팔로우할 사용자 ID") @PathVariable final Long followingUserId,
            @RequestBody @Valid final UserFollowRequest request) {
        return BaseResponse.ok(UserFollowResponse.of(userFollowUsecase.changeFollowingState(
                UserFollowRequest.toCommand(userId, followingUserId, request.type())
        )));
    }
}
