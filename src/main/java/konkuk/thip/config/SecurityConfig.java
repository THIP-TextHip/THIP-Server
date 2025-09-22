package konkuk.thip.config;

import konkuk.thip.common.security.constant.SecurityWhitelist;
import konkuk.thip.common.security.filter.JwtAuthenticationEntryPoint;
import konkuk.thip.common.security.filter.JwtAuthenticationFilter;
import konkuk.thip.common.security.oauth2.CustomOAuth2UserService;
import konkuk.thip.common.security.oauth2.CustomSuccessHandler;
import konkuk.thip.common.security.resolver.CustomAuthorizationRequestResolver;
import konkuk.thip.config.properties.WebDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

import static konkuk.thip.common.security.constant.AuthParameters.JWT_HEADER_KEY;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${server.https-url}")
    private String prodServerUrl;

    @Value("${server.http-url}")
    private String devServerUrl;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final WebDomainProperties webDomainProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // redirect_url(origin) → additionalParameters.return_to 저장
        var resolver = new CustomAuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization",
                webDomainProperties
        );

        // 세션 저장소
        var authReqRepo = new HttpSessionOAuth2AuthorizationRequestRepository();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login((oauth2) -> oauth2
                        .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                                .authorizationRequestResolver(resolver)
                                .authorizationRequestRepository(authReqRepo)
                        )
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customSuccessHandler) // OAuth2 로그인 성공 시 처리
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityWhitelist.patterns()).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handler -> handler.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = webDomainProperties.getWebDomainUrls();
        allowedOrigins.addAll(List.of(prodServerUrl, devServerUrl));

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        config.setExposedHeaders(Collections.singletonList(JWT_HEADER_KEY.getValue()));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}