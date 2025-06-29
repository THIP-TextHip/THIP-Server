package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
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

@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserSignupUseCase userSignupUseCase;
    private final VerifyNicknameUseCase verifyNicknameUseCase;

    @PostMapping("/users/signup")
    public BaseResponse<PostUserSignupResponse> signup(@Validated @RequestBody PostUserSignupRequest request) {
        return BaseResponse.ok(PostUserSignupResponse.of(
                userSignupUseCase.signup(request.toCommand()))
        );
    }

    @PostMapping("/users/nickname")
    public BaseResponse<PostUserVerifyNicknameResponse> verifyNickname(@Validated @RequestBody PostUserVerifyNicknameRequest request) {
        return BaseResponse.ok(PostUserVerifyNicknameResponse.of(
                verifyNicknameUseCase.isNicknameUnique(request.nickname()))
        );
    }
}
