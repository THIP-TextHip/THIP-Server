package konkuk.thip.common.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static konkuk.thip.common.security.constant.AuthParameters.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final int COOKIE_MAX_AGE = 60 * 60 * 24; // 1일
    private final LoginTokenStorage loginTokenStorage;

    @Value("${server.web-redirect-url}")
    private String webRedirectUrl;

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        LoginUser loginUser = oAuth2User.getLoginUser();

        if (oAuth2User.isNewUser()) {
            // 신규 유저 - 회원가입용 임시 토큰
            String tempToken = jwtUtil.createSignupToken(loginUser.oauth2Id());
//            addTokenCookie(response, tempToken);

            String loginTokenKey = UUID.randomUUID().toString();
            loginTokenStorage.put(loginTokenKey, TokenType.TEMP, tempToken, Duration.ofMinutes(5));      // ttl 5분

            getRedirectStrategy().sendRedirect(request, response, webRedirectUrl + REDIRECT_SIGNUP_URL.getValue() + "?loginTokenKey=" + loginTokenKey);
        } else {
            // 기존 유저 - 로그인용 액세스 토큰
            String accessToken = jwtUtil.createAccessToken(loginUser.userId());
//            addTokenCookie(response, accessToken);

            String loginTokenKey = UUID.randomUUID().toString();
            loginTokenStorage.put(loginTokenKey, TokenType.ACCESS, accessToken, Duration.ofMinutes(5));      // ttl 5분

            getRedirectStrategy().sendRedirect(request, response, webRedirectUrl + REDIRECT_HOME_URL.getValue() + "?loginTokenKey=" + loginTokenKey);
        }
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(JWT_HEADER_KEY.getValue(), token);
        if(webRedirectUrl.startsWith(HTTPS_PREFIX.getValue())) {
            cookie.setDomain(webRedirectUrl.replace(HTTPS_PREFIX.getValue(), ""));
        } else {
            cookie.setDomain("localhost");
        }
        cookie.setSecure(webRedirectUrl.startsWith(HTTPS_PREFIX.getValue()));
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }
}
