package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.application.port.in.dto.AliasChoiceViewResult;

public interface AliasRepositoryCustom {

    AliasChoiceViewResult getAllAliasesAndCategories();
}
