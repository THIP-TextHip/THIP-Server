package konkuk.thip.user.adapter.in.web.response;

import konkuk.thip.user.application.port.in.dto.AliasChoiceViewResult;

import java.util.List;

public record ShowAliasChoiceResponse(List<AliasChoice> aliasChoices) {

    public static ShowAliasChoiceResponse of(AliasChoiceViewResult result) {
        List<AliasChoice> choices = result.aliasChoices().stream()
                .map(ac -> new AliasChoice(
                        ac.aliasId(),
                        ac.aliasName(),
                        ac.categoryName(),
                        ac.imageUrl(),
                        ac.color()
                ))
                .toList();
        return new ShowAliasChoiceResponse(choices);
    }

    public record AliasChoice(
            Long aliasId,
            String aliasName,
            String categoryName,
            String imageUrl,
            String color
    ) {}
}
