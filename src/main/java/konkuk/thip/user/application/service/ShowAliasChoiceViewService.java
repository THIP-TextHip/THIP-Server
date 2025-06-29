package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.ShowAliasChoiceViewUseCase;
import konkuk.thip.user.application.port.in.dto.AliasChoiceViewResult;
import konkuk.thip.user.application.port.out.AliasQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowAliasChoiceViewService implements ShowAliasChoiceViewUseCase {

    private final AliasQueryPort aliasQueryPort;

    @Override
    public AliasChoiceViewResult getAllAliasesAndCategories() {
        return aliasQueryPort.getAllAliasesAndCategories();
    }
}
