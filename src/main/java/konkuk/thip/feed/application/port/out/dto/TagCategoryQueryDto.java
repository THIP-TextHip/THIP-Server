package konkuk.thip.feed.application.port.out.dto;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.annotations.QueryProjection;

public record TagCategoryQueryDto(
        String categoryValue,
        String tagValue
) {
    @QueryProjection
    public TagCategoryQueryDto {
        Assert.notNull(categoryValue, "categoryValue must not be null");
        Assert.notNull(tagValue, "tagValue must not be null");
    }
}
