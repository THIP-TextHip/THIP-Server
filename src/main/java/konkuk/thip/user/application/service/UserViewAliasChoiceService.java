package konkuk.thip.user.application.service;

import konkuk.thip.common.util.EnumMappings;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.application.port.in.UserViewAliasChoiceUseCase;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.domain.value.Alias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserViewAliasChoiceService implements UserViewAliasChoiceUseCase {

    @Override
    @Transactional(readOnly = true)
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        Map<Alias, Category> aliasToCategory = EnumMappings.getAliasToCategory();

        return new UserViewAliasChoiceResult(
                aliasToCategory.entrySet().stream()
                        .map(entry -> new UserViewAliasChoiceResult.AliasChoice(
                                entry.getKey().getValue(),
                                entry.getValue().getValue(),
                                entry.getKey().getImageUrl(),
                                entry.getKey().getColor()
                        )).toList()
        );
    }
}
