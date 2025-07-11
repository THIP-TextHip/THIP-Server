package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Feed extends BaseDomainEntity {

    private Long id;

    private String content;

    private Long creatorId;

    private Boolean isPublic;

    private Integer reportCount;

    private Integer likeCount = 0;

    private Integer commentCount = 0;

    private Long targetBookId;

    public static Feed withoutId(
            String content,
            Long creatorId,
            Boolean isPublic,
            Integer reportCount,
            Long targetBookId
    ) {
        return Feed.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .isPublic(isPublic)
                .reportCount(reportCount)
                .targetBookId(targetBookId)
                .likeCount(0)
                .commentCount(0)
                .build();
    }

}
