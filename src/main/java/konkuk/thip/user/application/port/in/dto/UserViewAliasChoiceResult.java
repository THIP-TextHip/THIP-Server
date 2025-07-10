package konkuk.thip.user.application.port.in.dto;

import java.util.List;

public record UserViewAliasChoiceResult(List<AliasChoice> aliasChoices) {

    public record AliasChoice(
            String aliasName,
            String categoryName,
            String imageUrl,
            String color
    ) {}
}
