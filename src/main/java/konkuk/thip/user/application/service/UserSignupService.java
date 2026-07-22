package konkuk.thip.user.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.security.oauth2.apple.AppleRefreshTokenStore;
import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.in.dto.UserSignupResult;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.value.Alias;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.user.domain.value.UserRole.USER;


@Service
@RequiredArgsConstructor
public class UserSignupService implements UserSignupUseCase {

    private final UserCommandPort userCommandPort;
    private final UserQueryPort userQueryPort;
    private final UserJpaRepository userJpaRepository;
    private final AppleRefreshTokenStore appleRefreshTokenStore;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserSignupResult signup(UserSignupCommand command) {
        Alias alias = Alias.from(command.aliasName());
        User user = User.withoutId(
                command.nickname(), USER.getType(), command.oauth2Id(), alias
        );

        // 이미 가입된 사용자인지 확인
        boolean isExistedUser = userQueryPort.existsByOauth2Id(command.oauth2Id());
        if (isExistedUser) {
            throw new BusinessException(ErrorCode.USER_ALREADY_SIGNED_UP);
        }

        Long userId = userCommandPort.save(user);

        // Apple 유저라면 Redis에 임시 보관된 refresh_token을 DB로 옮김
        if (command.oauth2Id().startsWith("apple_")) {
            String refreshToken = appleRefreshTokenStore.pop(command.oauth2Id());
            if (refreshToken != null) {
                userJpaRepository.findByOauth2Id(command.oauth2Id())
                        .ifPresent(entity -> {
                            entity.updateAppleRefreshToken(refreshToken);
                            userJpaRepository.save(entity);
                        });
            }
        }

        String accessToken = jwtUtil.createAccessToken(userId);
        return UserSignupResult.of(userId, accessToken);
    }
}
