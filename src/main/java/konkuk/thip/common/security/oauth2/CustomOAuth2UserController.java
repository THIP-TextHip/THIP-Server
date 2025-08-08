package konkuk.thip.common.security.oauth2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.oauth2.dto.OAuth2TokenRequest;
import konkuk.thip.common.security.oauth2.dto.OAuth2TokenResponse;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class CustomOAuth2UserController {

    private final UserJpaRepository userJpaRepository;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "소셜 로그인 유저 확인",
            description = "소셜 로그인 시 기존 유저인지 신규 유저인지 확인하고, AccessToken 또는 SignupToken을 발급합니다." +
            "isNewUser가 true인 경우 신규 유저로 간주하며(임시 토큰 발급), false인 경우 기존 유저로 간주합니다.(액세스 토큰 발급)"
    )
    @PostMapping("/oauth2/users")
    public BaseResponse<OAuth2TokenResponse> checkUserExists(
            @Parameter(description = "소셜 로그인 ID (형식: {provider}_{식별자 ID})", example = "kakao_1234567890")
            @RequestBody OAuth2TokenRequest oAuth2TokenRequest) throws IOException {
        return userJpaRepository.findByOauth2Id(oAuth2TokenRequest.oauth2Id())
                .map(user -> {
                    // 기존 유저: AccessToken 발급
                    String accessToken = jwtUtil.createAccessToken(user.getUserId());
                    return BaseResponse.ok(OAuth2TokenResponse.of(accessToken,false));
                })
                .orElseGet(() -> {
                    // 신규 유저: SignupToken 발급
                    String tempToken = jwtUtil.createSignupToken(oAuth2TokenRequest.oauth2Id());
                    return BaseResponse.ok(OAuth2TokenResponse.of(tempToken, true));
                });
    }
}
