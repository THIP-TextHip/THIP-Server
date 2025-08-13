package konkuk.thip.common.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

import static konkuk.thip.common.security.constant.AuthParameters.COOKIE_ACCESS_TOKEN;
import static konkuk.thip.common.security.constant.AuthParameters.COOKIE_TEMP_TOKEN;

@RestController
@RequiredArgsConstructor
public class AuthCookieController {

    private final LoginTokenStorage loginTokenStorage;
    private final JwtUtil jwtUtil;

    @PostMapping("/api/set-cookie")
    public ResponseEntity<?> setCookie(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String loginTokenKey = body.get("loginTokenKey");

        if (loginTokenKey == null || loginTokenKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        LoginTokenStorage.Entry entry = loginTokenStorage.consume(loginTokenKey);

        if (entry == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (entry.getType() == TokenType.ACCESS) {
            ResponseCookie cookie = ResponseCookie.from(COOKIE_ACCESS_TOKEN.getValue(), entry.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(Duration.ofDays(30))        // 30일짜리 쿠키
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(Map.of("type", TokenType.ACCESS.getValue()));
        } else {
            ResponseCookie cookie = ResponseCookie.from(COOKIE_TEMP_TOKEN.getValue(), entry.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")      // 일단 모든
                    .maxAge(Duration.ofMinutes(10))     // 10분짜리 쿠키
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(Map.of("type", TokenType.TEMP.getValue()));
        }
    }

//    @PostMapping("/api/exchange-temp-token")
//    public ResponseEntity<Void> exchangeTempToken(
//            HttpServletRequest request,
//            @RequestBody Map<String, String> body,
//            HttpServletResponse response
//    ) {
//        String tempCookieName = COOKIE_TEMP_TOKEN.getValue();
//        String tempToken = null;
//        if (request.getCookies() != null) {
//            for (jakarta.servlet.http.Cookie c : request.getCookies()) {
//                if (tempCookieName.equals(c.getName())) {
//                    tempToken = c.getValue();
//                    break;
//                }
//            }
//        }
//
//        // 1) tempToken 존재 확인
//        if (tempToken == null || tempToken.isBlank()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//
//        // 2) (선택) tempToken 유효성 검증 로직이 있다면 활성화
//        //    예) jwtUtil.validateSignupToken(signupToken);
//        //    예) String oauth2Id = jwtUtil.getOauth2IdFromSignupToken(signupToken);
//
//        // 3) 회원가입 완료된 userId로 access token 발급
//        String userIdStr = body.get("userId");
//        if (userIdStr == null || userIdStr.isBlank()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//        Long userId = Long.valueOf(userIdStr);
//
//        String accessToken = jwtUtil.createAccessToken(userId);
//
//        // 4) tempToken 삭제 + access_token 설정
//        ResponseCookie deleteSignup = ResponseCookie.from(COOKIE_TEMP_TOKEN.getValue(), "")
//                .httpOnly(true)
//                .secure(true)
//                .sameSite("None")
//                .path("/")
//                .maxAge(0)
//                .build();
//
//        ResponseCookie accessCookie = ResponseCookie.from(COOKIE_ACCESS_TOKEN.getValue(), accessToken)
//                .httpOnly(true)
//                .secure(true)
//                .sameSite("None")
//                .path("/")
//                .maxAge(Duration.ofDays(30))
//                .build();
//
//        response.addHeader("Set-Cookie", deleteSignup.toString());
//        response.addHeader("Set-Cookie", accessCookie.toString());
//
//        return ResponseEntity.noContent().build();
//    }
}
