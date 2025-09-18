package konkuk.thip.common.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.config.properties.WebDomainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static konkuk.thip.common.security.constant.AuthParameters.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginTokenStorage loginTokenStorage;

    private final WebDomainProperties webDomainProperties;

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // Resolver에서 세션에 저장한 origin을 복원
        String webRedirectDomain = null;
        if (request.getSession(false) != null) {
            webRedirectDomain = (String) request.getSession(false).getAttribute(REDIRECT_SESSION_KEY.getValue());
            request.getSession(false).removeAttribute(REDIRECT_SESSION_KEY.getValue()); // 사용했으면 제거(일회성)
        }

        // 허용 오리진 검증 및 폴백
        if (!webDomainProperties.isAllowed(Objects.toString(webRedirectDomain, ""))) {
            List<String> origins = webDomainProperties.getWebDomainUrls();
            if (origins == null || origins.isEmpty()) {
                throw new AuthException(ErrorCode.WEB_DOMAIN_ORIGIN_EMPTY);
            }
            webRedirectDomain = origins.get(0);
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        LoginUser loginUser = oAuth2User.getLoginUser();

        if (oAuth2User.isNewUser()) {
            // 신규 유저 - 회원가입용 임시 토큰
            String tempToken = jwtUtil.createSignupToken(loginUser.oauth2Id());

            String loginTokenKey = UUID.randomUUID().toString();
            loginTokenStorage.put(loginTokenKey, TokenType.TEMP, tempToken, Duration.ofMinutes(5));      // ttl 5분

            getRedirectStrategy().sendRedirect(request, response, webRedirectDomain + REDIRECT_SIGNUP_URL.getValue() + "?loginTokenKey=" + loginTokenKey);
        } else {
            // 기존 유저 - 로그인용 액세스 토큰
            String accessToken = jwtUtil.createAccessToken(loginUser.userId());

            String loginTokenKey = UUID.randomUUID().toString();
            loginTokenStorage.put(loginTokenKey, TokenType.ACCESS, accessToken, Duration.ofMinutes(5));      // ttl 5분

            getRedirectStrategy().sendRedirect(request, response, webRedirectDomain + REDIRECT_HOME_URL.getValue() + "?loginTokenKey=" + loginTokenKey);
        }
    }
}
