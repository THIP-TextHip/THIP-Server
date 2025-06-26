package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.out.AliasCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.Alias;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSignupService implements UserSignupUseCase {

    private static final String NORMAL_USER_ROLE = "일반유저";

    private final UserCommandPort userCommandPort;
    private final AliasCommandPort aliasCommandPort;

    @Override
    public Long signup(UserSignupCommand command) {
        Alias alias = aliasCommandPort.findById(command.getAliasId());
        User user = User.withoutId(
                command.getEmail(), command.getNickname(), alias.getImageUrl(), NORMAL_USER_ROLE, alias.getId()
        );

        return userCommandPort.save(user);
    }
}
