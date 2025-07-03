package konkuk.thip.vote.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Vote extends BaseDomainEntity {

    private Long id;

    private String content;

    private Long creatorId;

    private Integer page;

    private boolean isOverview;

    private Long roomId;

    public static Vote withoutId(String content, Long creatorId, Integer page, boolean isOverview, Long roomId) {
        return Vote.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .page(page)
                .isOverview(isOverview)
                .roomId(roomId)
                .build();
    }
}
