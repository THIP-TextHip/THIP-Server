package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.AliasQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AliasQueryPersistenceAdapter implements AliasQueryPort {

    private final AliasJpaRepository aliasJpaRepository;

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        return aliasJpaRepository.getAllAliasesAndCategories();
    }
}
