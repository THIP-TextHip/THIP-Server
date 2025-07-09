package konkuk.thip.feed.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Tag {

    private TagName value;

    public static Tag from(String tagName) {
        return Tag.builder()
                .value(TagName.from(tagName))
                .build();
    }

}
