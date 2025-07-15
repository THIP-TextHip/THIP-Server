package konkuk.thip.user.adapter.in.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.Oauth2Id;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.adapter.in.web.request.UserFollowRequest;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.request.UserVerifyNicknameRequest;
import konkuk.thip.user.adapter.in.web.response.UserFollowResponse;
import konkuk.thip.user.adapter.in.web.response.UserSignupResponse;
import konkuk.thip.user.adapter.in.web.response.UserVerifyNicknameResponse;
import konkuk.thip.user.application.port.in.UserFollowUsecase;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.UserVerifyNicknameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.security.constant.AuthParameters.JWT_HEADER_KEY;
import static konkuk.thip.common.security.constant.AuthParameters.JWT_PREFIX;

@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserSignupUseCase userSignupUseCase;
    private final UserVerifyNicknameUseCase userVerifyNicknameUseCase;
    private final UserFollowUsecase userFollowUsecase;
    private final JwtUtil jwtUtil;

    @PostMapping("/users/signup")
    public BaseResponse<UserSignupResponse> signup(@Valid @RequestBody final UserSignupRequest request,
                                                   @Oauth2Id final String oauth2Id,
                                                   HttpServletResponse response) {
        Long userId = userSignupUseCase.signup(request.toCommand(oauth2Id));
        String accessToken = jwtUtil.createAccessToken(userId);
        response.setHeader(JWT_HEADER_KEY.getValue(), JWT_PREFIX.getValue() + accessToken);
        return BaseResponse.ok(UserSignupResponse.of(userId));
    }

    @PostMapping("/users/nickname")
    public BaseResponse<UserVerifyNicknameResponse> verifyNickname(@Valid @RequestBody final UserVerifyNicknameRequest request) {
        return BaseResponse.ok(UserVerifyNicknameResponse.of(
                userVerifyNicknameUseCase.isNicknameUnique(request.nickname()))
        );
    }

    // 팔루우 상태 변경 : true -> 팔로우, false -> 언팔로우
    @PostMapping("/users/following/{followingUserId}")
    public BaseResponse<UserFollowResponse> followUser(@UserId final Long userId,
                                            @PathVariable final Long followingUserId,
                                            @RequestBody @Valid final UserFollowRequest request) {
        return BaseResponse.ok(UserFollowResponse.of(userFollowUsecase.changeFollowingState(
                UserFollowRequest.toCommand(userId, followingUserId, request.type())
        )));
    }
}
