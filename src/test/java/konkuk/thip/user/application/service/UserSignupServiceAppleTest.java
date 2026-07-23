package konkuk.thip.user.application.service;

import konkuk.thip.common.security.oauth2.apple.AppleRefreshTokenStore;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[단위] Apple 유저 회원가입 서비스 테스트")
class UserSignupServiceAppleTest {

    @Autowired
    private UserSignupService userSignupService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AppleRefreshTokenStore appleRefreshTokenStore;

    @Test
    @DisplayName("Apple 유저 회원가입 시 Redis에 저장된 refresh_token이 DB로 옮겨진다")
    void signup_appleUser_movesRefreshTokenFromRedisToDb() {
        // given
        String oauth2Id = "apple_test_sub_12345";
        String refreshToken = "apple_refresh_token_value";
        appleRefreshTokenStore.save(oauth2Id, refreshToken);

        UserSignupCommand command = UserSignupCommand.builder()
                .oauth2Id(oauth2Id)
                .nickname("테스트닉네임")
                .aliasName(Alias.WRITER.getValue())
                .build();

        // when
        userSignupService.signup(command);

        // then
        UserJpaEntity saved = userJpaRepository.findByOauth2Id(oauth2Id).orElseThrow();
        assertThat(saved.getAppleRefreshToken()).isEqualTo(refreshToken);
        assertThat(appleRefreshTokenStore.pop(oauth2Id)).isNull(); // Redis에서 삭제됨
    }

    @Test
    @DisplayName("Apple 유저 회원가입 시 Redis에 refresh_token이 없으면 DB에도 저장되지 않는다")
    void signup_appleUser_withoutRedisToken_doesNotSaveToDb() {
        // given
        String oauth2Id = "apple_test_sub_no_token";
        UserSignupCommand command = UserSignupCommand.builder()
                .oauth2Id(oauth2Id)
                .nickname("테스트닉네임2")
                .aliasName(Alias.WRITER.getValue())
                .build();

        // when
        userSignupService.signup(command);

        // then
        UserJpaEntity saved = userJpaRepository.findByOauth2Id(oauth2Id).orElseThrow();
        assertThat(saved.getAppleRefreshToken()).isNull();
    }

    @Test
    @DisplayName("Apple 유저가 아닌 경우 Redis 조회 없이 회원가입이 완료된다")
    void signup_nonAppleUser_doesNotCheckRedis() {
        // given
        UserSignupCommand command = UserSignupCommand.builder()
                .oauth2Id("kakao_99999")
                .nickname("카카오유저")
                .aliasName(Alias.WRITER.getValue())
                .build();

        // when
        userSignupService.signup(command);

        // then
        UserJpaEntity saved = userJpaRepository.findByOauth2Id("kakao_99999").orElseThrow();
        assertThat(saved.getAppleRefreshToken()).isNull();
    }
}
