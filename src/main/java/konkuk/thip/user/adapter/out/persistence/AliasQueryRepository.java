package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;

public interface AliasQueryRepository {

    UserViewAliasChoiceResult getAllAliasesAndCategories();
}
