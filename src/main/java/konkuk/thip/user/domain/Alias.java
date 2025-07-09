package konkuk.thip.user.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Alias {

    private AliasName value;

    private String imageUrl;

    private String color;

    public static Alias from(String value, String imageUrl, String color) {
        return Alias.builder()
                .value(AliasName.from(value))
                .imageUrl(imageUrl)
                .color(color)
                .build();
    }
}
