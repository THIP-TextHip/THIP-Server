package konkuk.thip.user.adapter.in.web.response;

import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;

import java.util.List;

public record UserViewAliasChoiceResponse(List<AliasChoice> aliasChoices) {

    public static UserViewAliasChoiceResponse of(UserViewAliasChoiceResult result) {
        List<AliasChoice> choices = result.aliasChoices().stream()
                .map(ac -> new AliasChoice(
                        ac.aliasName(),
                        ac.categoryName(),
                        ac.imageUrl(),
                        ac.color()
                ))
                .toList();
        return new UserViewAliasChoiceResponse(choices);
    }

    public record AliasChoice(
            String aliasName,
            String categoryName,
            String imageUrl,
            String color
    ) {}
}
