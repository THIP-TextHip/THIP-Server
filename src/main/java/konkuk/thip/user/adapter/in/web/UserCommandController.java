package konkuk.thip.user.adapter.in.web;

import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.user.adapter.in.web.request.UserSignupRequest;
import konkuk.thip.user.adapter.in.web.response.UserSignupResponse;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserCommandController {

    private final UserSignupUseCase userSignupUseCase;

    @PostMapping("/users/signup")
    public BaseResponse<UserSignupResponse> signup(@Validated @RequestBody UserSignupRequest request) {
        return BaseResponse.ok(UserSignupResponse.of(
                userSignupUseCase.signup(request.toCommand()))
        );
    }
}
