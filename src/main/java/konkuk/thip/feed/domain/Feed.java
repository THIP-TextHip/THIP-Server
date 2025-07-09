package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class Feed extends BaseDomainEntity {

    private Long id;

    private String content;

    private Long creatorId;

    private Boolean isPublic;

    private int reportCount;

    private Long targetBookId;

    private List<Tag> tagList;

    public static Feed withoutId(String content, Long creatorId, Boolean isPublic, int reportCount, Long targetBookId, List<Tag> tagList) {
        return Feed.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .isPublic(isPublic)
                .reportCount(reportCount)
                .targetBookId(targetBookId)
                .tagList(tagList)
                .build();
    }

}
