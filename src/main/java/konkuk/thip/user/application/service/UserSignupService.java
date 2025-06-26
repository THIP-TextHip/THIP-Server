package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.out.AliasCommandPort;
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
    private final AliasCommandPort aliasCommandPort;

    @Override
    @Transactional
    public Long signup(UserSignupCommand command) {
        Alias alias = aliasCommandPort.findById(command.aliasId());
        User user = User.withoutId(
                command.email(), command.nickname(), alias.getImageUrl(), USER.getType(), alias.getId()
        );

        return userCommandPort.save(user);
    }
}
