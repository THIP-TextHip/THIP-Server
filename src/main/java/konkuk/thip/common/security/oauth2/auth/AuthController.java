package konkuk.thip.common.security.oauth2.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.security.oauth2.LoginTokenStorage;
import konkuk.thip.common.security.oauth2.TokenType;
import konkuk.thip.common.security.oauth2.auth.dto.AuthSetCookieRequest;
import konkuk.thip.common.security.oauth2.auth.dto.AuthSetCookieResponse;
import konkuk.thip.common.security.oauth2.auth.dto.AuthTokenRequest;
import konkuk.thip.common.security.oauth2.auth.dto.AuthTokenResponse;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;
import static konkuk.thip.common.exception.code.ErrorCode.AUTH_INVALID_LOGIN_TOKEN_KEY;
import static konkuk.thip.common.security.constant.AuthParameters.COOKIE_ACCESS_TOKEN;
import static konkuk.thip.common.security.constant.AuthParameters.COOKIE_TEMP_TOKEN;

@Tag(name = "Auth API", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserJpaRepository userJpaRepository;
    private final JwtUtil jwtUtil;

    private final LoginTokenStorage loginTokenStorage;

    @Value(("${server.profile}"))
    private String profile;

    @Operation(
            summary = "소셜 로그인 유저 확인",
            description = "소셜 로그인 시 기존 유저인지 신규 유저인지 확인하고, AccessToken 또는 SignupToken을 발급합니다." +
            "isNewUser가 true인 경우 신규 유저로 간주하며(임시 토큰 발급), false인 경우 기존 유저로 간주합니다.(액세스 토큰 발급)"
    )
    @PostMapping("/users")
    public BaseResponse<AuthTokenResponse> checkUserExists(
            @Parameter(description = "소셜 로그인 ID (형식: {provider}_{식별자 ID})", example = "kakao_1234567890")
            @RequestBody AuthTokenRequest authTokenRequest) throws IOException {
        return userJpaRepository.findByOauth2Id(authTokenRequest.oauth2Id())
                .map(user -> {
                    // 기존 유저: AccessToken 발급
                    String accessToken = jwtUtil.createAccessToken(user.getUserId());
                    return BaseResponse.ok(AuthTokenResponse.of(accessToken,false));
                })
                .orElseGet(() -> {
                    // 신규 유저: SignupToken 발급
                    String tempToken = jwtUtil.createSignupToken(authTokenRequest.oauth2Id());
                    return BaseResponse.ok(AuthTokenResponse.of(tempToken, true));
                });
    }

    @Operation(
            summary = "쿠키 설정",
            description = "로그인 토큰 키를 사용하여 쿠키를 설정합니다. AccessToken 또는 SignupToken을 쿠키로 설정합니다."
    )
    @PostMapping("/set-cookie")
    public BaseResponse<AuthSetCookieResponse> setCookie(
            @RequestBody AuthSetCookieRequest request,
            HttpServletResponse response
    ) {
        String loginTokenKey = request.loginTokenKey();
        if (loginTokenKey == null || loginTokenKey.isBlank()) {
            throw new AuthException(API_INVALID_PARAM,
                    new IllegalArgumentException("loginTokenKey는 필수 파라미터입니다."));
        }

        LoginTokenStorage.Entry entry = loginTokenStorage.consume(loginTokenKey);
        if (entry == null) {
            throw new AuthException(AUTH_INVALID_LOGIN_TOKEN_KEY);
        }

        ResponseCookie cookie;
        String type;

        if (entry.getType() == TokenType.ACCESS) {
            cookie = ResponseCookie.from(COOKIE_ACCESS_TOKEN.getValue(), entry.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();
            type = TokenType.ACCESS.getValue();
        } else {
            cookie = ResponseCookie.from(COOKIE_TEMP_TOKEN.getValue(), entry.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(Duration.ofMinutes(10))
                    .build();
            type = TokenType.TEMP.getValue();
        }

        response.addHeader("Set-Cookie", cookie.toString());
        return BaseResponse.ok(AuthSetCookieResponse.of(type));
    }

    @PostMapping("/exchange-temp-token")
    public ResponseEntity<Void> exchangeTempToken(
            HttpServletRequest request,
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {
        String tempCookieName = COOKIE_TEMP_TOKEN.getValue();
        String tempToken = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie c : request.getCookies()) {
                if (tempCookieName.equals(c.getName())) {
                    tempToken = c.getValue();
                    break;
                }
            }
        }

        // 1) tempToken 존재 확인
        if (tempToken == null || tempToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 2) (선택) tempToken 유효성 검증 로직이 있다면 활성화
        //    예) jwtUtil.validateSignupToken(signupToken);
        //    예) String oauth2Id = jwtUtil.getOauth2IdFromSignupToken(signupToken);

        // 3) 회원가입 완료된 userId로 access token 발급
        String userIdStr = body.get("userId");
        if (userIdStr == null || userIdStr.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Long userId = Long.valueOf(userIdStr);

        String accessToken = jwtUtil.createAccessToken(userId);

        // 4) tempToken 삭제 + access_token 설정
        ResponseCookie deleteSignup = ResponseCookie.from(COOKIE_TEMP_TOKEN.getValue(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie accessCookie = ResponseCookie.from(COOKIE_ACCESS_TOKEN.getValue(), accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader("Set-Cookie", deleteSignup.toString());
        response.addHeader("Set-Cookie", accessCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
