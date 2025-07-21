package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.*;

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

    @Builder.Default
    private List<Content> contentList = new ArrayList<>();

    public static Feed withoutId(String content, Long creatorId, Boolean isPublic, Long targetBookId,
                                 List<String> tagValues, List<String> imageUrls) {

        return Feed.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .isPublic(isPublic)
                .reportCount(0)
                .likeCount(0)
                .commentCount(0)
                .targetBookId(targetBookId)
                .tagList(Tag.fromList(tagValues))
                .contentList(convertToContentList(imageUrls))
                .build();
    }

    private static List<Content> convertToContentList(List<String> imageUrls) {
        if (imageUrls == null) return new ArrayList<>();
        return imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> Content.builder().contentUrl(url).build())
                .collect(Collectors.toList());
    }

    public static void validateTags(List<String> tagList) {
        boolean tagListEmpty = (tagList == null || tagList.isEmpty());

        // 태그가 있는 경우, 개수 최대 5개 제한
        if (!tagListEmpty && tagList.size() > 5) {
            throw new InvalidStateException(INVALID_FEED_COMMAND, new IllegalArgumentException("태그는 최대 5개까지 입력할 수 있습니다."));
        }

        // 태그 중복 체크
        if (!tagListEmpty) {
            long distinctCount = tagList.stream().distinct().count();
            if (distinctCount != tagList.size()) {
                throw new InvalidStateException(INVALID_FEED_COMMAND, new IllegalArgumentException("태그는 중복 될 수 없습니다."));
            }
        }
    }

    public static void validateImageCount(int imageSize) {
        if (imageSize > 3) {
            throw new InvalidStateException(INVALID_FEED_COMMAND, new IllegalArgumentException("이미지는 최대 3개까지 업로드할 수 있습니다."));
        }
    }

    public void validateCreator(Long userId) {
        if (!this.creatorId.equals(userId)) {
            throw new InvalidStateException(FEED_UPDATE_FORBIDDEN);
        }
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void updateVisibility(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void updateTags(List<String> tagValues) {
        this.tagList = Tag.fromList(tagValues);
    }

    public void updateImages(List<String> imageUrls) {
        this.contentList = convertToContentList(imageUrls);
    }

    public void validateOwnsImages(List<String> candidateImageUrls) {
        Set<String> myImageUrls = this.getContentList().stream()
                .map(Content::getContentUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toSet());
        for (String url : candidateImageUrls) {
            if (!myImageUrls.contains(url)) {
                throw new InvalidStateException(INVALID_FEED_COMMAND, new IllegalArgumentException("해당 이미지는 이 피드에 존재하지 않습니다: " + url));
            }
        }
    }

}
