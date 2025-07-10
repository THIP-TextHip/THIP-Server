package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserViewAliasChoiceUseCase;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserViewAliasChoiceService implements UserViewAliasChoiceUseCase {

    private final UserQueryPort userQueryPort;

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        return userQueryPort.getAllAliasesAndCategories();
    }
}
