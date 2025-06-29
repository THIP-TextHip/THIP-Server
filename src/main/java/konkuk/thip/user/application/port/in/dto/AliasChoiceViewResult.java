package konkuk.thip.user.application.port.in.dto;

import java.util.List;

public record AliasChoiceViewResult(List<AliasChoice> aliasChoices) {

    public record AliasChoice(
            Long aliasId,
            String aliasName,
            String categoryName,
            String imageUrl,
            String color
    ) {}
}
