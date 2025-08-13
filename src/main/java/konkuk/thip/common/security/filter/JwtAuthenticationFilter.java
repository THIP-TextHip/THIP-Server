package konkuk.thip.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.security.oauth2.CustomOAuth2User;
import konkuk.thip.common.security.oauth2.LoginUser;
import konkuk.thip.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.common.security.constant.AuthParameters.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token == null) {
                throw new AuthException(AUTH_TOKEN_NOT_FOUND);
            }

            if (!jwtUtil.validateToken(token)) {
                throw new AuthException(AUTH_INVALID_TOKEN);
            }

            if (jwtUtil.isExpired(token)) {
                throw new AuthException(AUTH_EXPIRED_TOKEN);
            }

            LoginUser loginUser = jwtUtil.getLoginUser(token);

            if (loginUser.userId() != null) {
                request.setAttribute(JWT_ACCESS_TOKEN_KEY.getValue(), loginUser.userId());
            }
            else {
                request.setAttribute(JWT_SIGNUP_TOKEN_KEY.getValue(), loginUser.oauth2Id());
            }

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(loginUser);

            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            log.error("JWT 필터에서 오류 발생: {}", e.getMessage());
            request.setAttribute("exception", e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Authorization 헤더 우선 (앱)
        String authorization = request.getHeader(JWT_HEADER_KEY.getValue());
        if (authorization != null && authorization.startsWith(JWT_PREFIX.getValue())) {
            return authorization.split(" ")[1];
        }

        // 2. Cookie에서 JWT 추출 (웹)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JWT_HEADER_KEY.getValue().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        log.info("토큰이 없습니다.");
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 화이트리스트 경로에 대해서는 JWT 필터 제외
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.startsWith("/oauth2/authorization")
                || path.startsWith("/login/oauth2/code")
                || path.startsWith("/oauth2/users")
                ;
    }

}
