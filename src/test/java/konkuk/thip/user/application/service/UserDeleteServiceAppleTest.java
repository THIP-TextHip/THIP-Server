package konkuk.thip.user.application.service;

import konkuk.thip.common.security.oauth2.apple.AppleTokenClient;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("[단위] Apple 유저 탈퇴 서비스 테스트")
class UserDeleteServiceAppleTest {

    @Autowired
    private UserDeleteService userDeleteService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @MockBean
    private AppleTokenClient appleTokenClient;

    @Test
    @DisplayName("Apple 유저 탈퇴 시 저장된 refresh_token으로 Apple revoke가 호출된다")
    void deleteUser_appleUser_revokesAppleToken() {
        // given
        UserJpaEntity user = TestEntityFactory.createUser(Alias.WRITER);
        user = userJpaRepository.save(user);
        user.updateAppleRefreshToken("apple_refresh_token_abc");
        userJpaRepository.save(user);

        // oauth2Id를 apple_로 시작하도록 반영하기 위해 직접 엔티티 수정
        // (TestEntityFactory.createUser는 kakao oauth2Id를 기본값으로 사용하므로 별도로 Apple 유저 생성)
        UserJpaEntity appleUser = UserJpaEntity.builder()
                .nickname("애플유저")
                .oauth2Id("apple_test_sub_delete")
                .alias(Alias.WRITER)
                .role(konkuk.thip.user.domain.value.UserRole.USER)
                .build();
        appleUser = userJpaRepository.save(appleUser);
        appleUser.updateAppleRefreshToken("apple_refresh_token_xyz");
        userJpaRepository.save(appleUser);

        Long userId = appleUser.getUserId();
        String fakeAuthToken = "Bearer fake.access.token";

        // when
        userDeleteService.deleteUser(userId, fakeAuthToken);

        // then
        verify(appleTokenClient, times(1)).revokeToken("apple_refresh_token_xyz");
    }

    @Test
    @DisplayName("Apple 유저라도 refresh_token이 없으면 revoke를 호출하지 않는다")
    void deleteUser_appleUser_withoutRefreshToken_doesNotRevoke() {
        // given
        UserJpaEntity appleUser = UserJpaEntity.builder()
                .nickname("애플유저2")
                .oauth2Id("apple_test_sub_no_token")
                .alias(Alias.WRITER)
                .role(konkuk.thip.user.domain.value.UserRole.USER)
                .build();
        appleUser = userJpaRepository.save(appleUser);
        Long userId = appleUser.getUserId();

        // when
        userDeleteService.deleteUser(userId, "Bearer fake.token");

        // then
        verify(appleTokenClient, never()).revokeToken(any());
    }

    @Test
    @DisplayName("Apple 유저가 아닌 경우 revoke를 호출하지 않는다")
    void deleteUser_nonAppleUser_doesNotRevoke() {
        // given
        UserJpaEntity kakaoUser = TestEntityFactory.createUser(Alias.WRITER);
        kakaoUser = userJpaRepository.save(kakaoUser);
        Long userId = kakaoUser.getUserId();

        // when
        userDeleteService.deleteUser(userId, "Bearer fake.token");

        // then
        verify(appleTokenClient, never()).revokeToken(any());
    }
}
