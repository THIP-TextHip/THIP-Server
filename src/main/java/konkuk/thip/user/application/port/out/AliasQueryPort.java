package konkuk.thip.user.application.port.out;

import konkuk.thip.user.application.port.in.dto.AliasChoiceViewResult;

public interface AliasQueryPort {

    AliasChoiceViewResult getAllAliasesAndCategories();
}
