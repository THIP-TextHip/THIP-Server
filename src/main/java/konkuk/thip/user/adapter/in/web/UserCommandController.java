package konkuk.thip.user.adapter.in.web;

import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.Oauth2Id;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.response.UserSignupResponse;
import konkuk.thip.user.adapter.in.web.request.PostUserSignupRequest;
import konkuk.thip.user.adapter.in.web.request.PostUserVerifyNicknameRequest;
import konkuk.thip.user.adapter.in.web.response.PostUserSignupResponse;
import konkuk.thip.user.adapter.in.web.response.PostUserVerifyNicknameResponse;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.VerifyNicknameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.security.constant.JwtAuthParameters.JWT_HEADER_KEY;
import static konkuk.thip.common.security.constant.JwtAuthParameters.JWT_PREFIX;

@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserSignupUseCase userSignupUseCase;
    private final VerifyNicknameUseCase verifyNicknameUseCase;
    private final JwtUtil jwtUtil;

    @PostMapping("/users/signup")
    public BaseResponse<UserSignupResponse> signup(@Validated @RequestBody UserSignupRequest request,
                                                   @Oauth2Id String oauth2Id,
                                                   HttpServletResponse response) {
        Long userId = userSignupUseCase.signup(request.toCommand(oauth2Id));
        String accessToken = jwtUtil.createAccessToken(userId);
        response.setHeader(JWT_HEADER_KEY.getValue(), JWT_PREFIX.getValue() + accessToken);
        return BaseResponse.ok(UserSignupResponse.of(userId));
    }

    @PostMapping("/users/nickname")
    public BaseResponse<PostUserVerifyNicknameResponse> verifyNickname(@Validated @RequestBody PostUserVerifyNicknameRequest request) {
        return BaseResponse.ok(PostUserVerifyNicknameResponse.of(
                verifyNicknameUseCase.isNicknameUnique(request.nickname()))
        );
    }
}
