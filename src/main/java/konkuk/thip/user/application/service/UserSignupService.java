package konkuk.thip.user.application.service;

import konkuk.thip.common.security.util.JwtUtil;
import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.in.dto.UserSignupResult;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.Alias;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.user.adapter.out.jpa.UserRole.USER;


@Service
@RequiredArgsConstructor
public class UserSignupService implements UserSignupUseCase {

    private final UserCommandPort userCommandPort;

    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserSignupResult signup(UserSignupCommand command) {
        Alias alias = Alias.from(command.aliasName());
        User user = User.withoutId(
                command.nickname(), USER.getType(), command.oauth2Id(), alias
        );
        Long userId = userCommandPort.save(user);
        String accessToken = jwtUtil.createAccessToken(userId);

        return UserSignupResult.of(userId, accessToken);
    }
}
