package konkuk.thip.common.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static konkuk.thip.common.security.constant.AuthParameters.JWT_HEADER_KEY;
import static konkuk.thip.common.security.constant.AuthParameters.JWT_PREFIX;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String CONTENT_TYPE = "application/json";
    private static final String ENCODING = "UTF-8";
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        LoginUser loginUser = oAuth2User.getLoginUser();

        if(oAuth2User.isNewUser()) {
            // 최초 로그인 : 회원가입을 위한 임시 토큰 발급
            String tempToken = jwtUtil.createSignupToken(loginUser.oauth2Id());
            response.setHeader(JWT_HEADER_KEY.getValue(), JWT_PREFIX.getValue() + tempToken);
            writeResponse(response, BaseResponse.ok(oAuth2User.getLoginUser()));
            return;
        }

        // 기존 회원 : Access Token 발급
        String accessToken = jwtUtil.createAccessToken(loginUser.userId());
        response.setHeader(JWT_HEADER_KEY.getValue(), JWT_PREFIX.getValue() + accessToken);
        writeResponse(response, BaseResponse.ok(oAuth2User.getLoginUser()));
    }

    private void writeResponse(HttpServletResponse response, Object value) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(ENCODING);
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(value);
        response.getWriter().write(body);
    }
}
