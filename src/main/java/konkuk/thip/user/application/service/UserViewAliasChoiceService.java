package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserViewAliasChoiceUseCase;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.AliasQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserViewAliasChoiceService implements UserViewAliasChoiceUseCase {

    private final AliasQueryPort aliasQueryPort;

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        return aliasQueryPort.getAllAliasesAndCategories();
    }
}
