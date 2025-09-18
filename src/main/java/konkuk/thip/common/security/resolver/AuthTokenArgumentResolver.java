package konkuk.thip.common.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import konkuk.thip.common.exception.AuthException;
import konkuk.thip.common.security.annotation.AuthToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static konkuk.thip.common.exception.code.ErrorCode.AUTH_TOKEN_NOT_FOUND;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthTokenArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthToken.class)
            && parameter.getParameterType().equals(String.class);
    }

    @Override
    public String resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Object token = ((HttpServletRequest) webRequest.getNativeRequest()).getAttribute("token");
        if (token == null) {
            throw new AuthException(AUTH_TOKEN_NOT_FOUND);
        }
        return (String) token;
    }
}
