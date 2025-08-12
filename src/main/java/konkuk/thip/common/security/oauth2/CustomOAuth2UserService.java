package konkuk.thip.common.security.oauth2;

import konkuk.thip.common.exception.AuthException;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.AUTH_UNSUPPORTED_SOCIAL_LOGIN;
import static konkuk.thip.common.security.constant.AuthParameters.GOOGLE;
import static konkuk.thip.common.security.constant.AuthParameters.KAKAO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("OAuth2User: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserDetails oAuth2UserDetails = null;
        if (registrationId.equals(KAKAO.getValue())) {
            oAuth2UserDetails = new KakaoUserDetails(oAuth2User.getAttributes());
        }
        else if (registrationId.equals(GOOGLE.getValue())) {
            oAuth2UserDetails = new GoogleUserDetails(oAuth2User.getAttributes());
        }
        else {
            log.warn("카카오 또는 구글 소셜 로그인만 지원합니다.");
            throw new AuthException(AUTH_UNSUPPORTED_SOCIAL_LOGIN);
        }

        String oauth2Id = oAuth2UserDetails.getProvider() + "_" + oAuth2UserDetails.getProviderId(); //kakao_1234567890
        Optional<UserJpaEntity> existingUser = userJpaRepository.findByOauth2Id(oauth2Id);
        if(existingUser.isEmpty()) {
            LoginUser newUser = LoginUser.createNewUser(oauth2Id);
            return new CustomOAuth2User(newUser);
        }

        LoginUser loginUser = LoginUser.createExistingUser(oauth2Id, existingUser.get().getUserId());
        return new CustomOAuth2User(loginUser);
    }
}

