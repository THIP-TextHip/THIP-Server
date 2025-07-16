package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

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

    private List<Content> contentList;

    public static Feed withoutId(String content, Long creatorId, Boolean isPublic, Long targetBookId, List<Tag> tagList, List<String> imageUrls) {

        return Feed.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .isPublic(isPublic)
                .reportCount(0)
                .likeCount(0)
                .commentCount(0)
                .targetBookId(targetBookId)
                .tagList(tagList != null ? tagList : List.of())
                .contentList(convertToContentList(imageUrls))
                .build();
    }

    private static List<Content> convertToContentList(List<String> imageUrls) {
        if (imageUrls == null) return List.of();

        return imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> Content.builder().contentUrl(url).build())
                .collect(Collectors.toList());
    }


}
