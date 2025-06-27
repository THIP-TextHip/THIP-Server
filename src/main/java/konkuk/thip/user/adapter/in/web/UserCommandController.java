package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.request.VerifyNicknameRequest;
import konkuk.thip.user.adapter.in.web.response.UserSignupResponse;
import konkuk.thip.user.adapter.in.web.response.VerifyNicknameResponse;
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
    public BaseResponse<UserSignupResponse> signup(@Validated @RequestBody UserSignupRequest request) {
        return BaseResponse.ok(UserSignupResponse.of(
                userSignupUseCase.signup(request.toCommand()))
        );
    }

    @PostMapping("/users/nickname")
    public BaseResponse<VerifyNicknameResponse> verifyNickname(@Validated @RequestBody VerifyNicknameRequest request) {
        return BaseResponse.ok(VerifyNicknameResponse.of(
                verifyNicknameUseCase.isNicknameUnique(request.nickname()))
        );
    }
}
