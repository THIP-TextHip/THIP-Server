package konkuk.thip.common.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import konkuk.thip.config.properties.WebDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static konkuk.thip.common.security.constant.AuthParameters.REDIRECT_SESSION_KEY;
import static konkuk.thip.common.security.constant.AuthParameters.REDIRECT_URL_KEY;

@RequiredArgsConstructor
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegate;
    private final WebDomainProperties webDomainProperties;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo,
                                              String authorizationRequestBaseUri,
                                              WebDomainProperties props) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
        this.webDomainProperties = props;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest base = delegate.resolve(request);
        return customize(request, base);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest base = delegate.resolve(request, clientRegistrationId);
        return customize(request, base);
    }

    private OAuth2AuthorizationRequest customize(HttpServletRequest request, OAuth2AuthorizationRequest base) {
        if (base == null) return null;

        String redirectUrl = request.getParameter(REDIRECT_URL_KEY.getValue());
        Map<String, Object> additional = new HashMap<>(base.getAdditionalParameters());
        var session = request.getSession(true);
        if (StringUtils.hasText(redirectUrl) && webDomainProperties.isAllowed(redirectUrl)) {
            session.setAttribute(REDIRECT_SESSION_KEY.getValue(), redirectUrl);
        } else {
            session.removeAttribute(REDIRECT_SESSION_KEY.getValue());
        }
        return OAuth2AuthorizationRequest.from(base)
                .additionalParameters(additional)
                .build();
    }
}