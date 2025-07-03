package konkuk.thip.user.application.port.out;

import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;

public interface AliasQueryPort {

    UserViewAliasChoiceResult getAllAliasesAndCategories();
}
