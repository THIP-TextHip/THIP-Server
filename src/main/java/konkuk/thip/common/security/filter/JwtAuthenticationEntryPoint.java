package konkuk.thip.common.security.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver")
                                       HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 필터에서 set한 예외 우선
        Exception original = (Exception) request.getAttribute("exception");
        if (original == null) {
            original = authException;
        }

        Exception mapped = wrapAsAuthException(original);

        resolver.resolveException(request, response, null, mapped);
    }

    // 모든 예외를 AuthException(401)으로 감싸는 메서드
    private Exception wrapAsAuthException(Exception e) {
        if (e instanceof AuthException) {
            return e;
        }
        return new AuthException(ErrorCode.AUTH_INTERNAL_SERVER_ERROR, e);
    }
}