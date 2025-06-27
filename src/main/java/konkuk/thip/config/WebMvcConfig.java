package konkuk.thip.config;

import konkuk.thip.common.security.argument_resolver.Oauth2IdArgumentResolver;
import konkuk.thip.common.security.argument_resolver.UserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserIdArgumentResolver userIdArgumentResolver;
    private final Oauth2IdArgumentResolver oauth2IdArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userIdArgumentResolver);
        resolvers.add(oauth2IdArgumentResolver);
    }
}
