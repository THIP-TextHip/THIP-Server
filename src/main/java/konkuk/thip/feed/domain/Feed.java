package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Builder;
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

    @Builder.Default
    private Integer reportCount = 0;

    @Builder.Default
    private Integer likeCount = 0;

    @Builder.Default
    private Integer commentCount = 0;

    private Long targetBookId;

    private List<Tag> tagList;

    public static Feed withoutId(String content, Long creatorId, Boolean isPublic, Long targetBookId, List<Tag> tagList) {
        return Feed.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .isPublic(isPublic)
                .reportCount(0)
                .likeCount(0)
                .commentCount(0)
                .targetBookId(targetBookId)
                .tagList(tagList)
                .build();
    }

}
