package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    private List<Content> contentList;

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
        if (imageUrls == null) return List.of();

        return imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> Content.builder().contentUrl(url).build())
                .collect(Collectors.toList());
    }

    public static void validateCategoryAndTags(String category, List<String> tagList) {

        // 둘 다 없으면 카테고리도 태그도 없는 새 게시글 (예외 상황 아님)
        boolean categoryEmpty = (category == null || category.trim().isEmpty());
        boolean tagListEmpty = (tagList == null || tagList.isEmpty());

        // 둘 중 하나만 입력된 경우
        if (categoryEmpty ^ tagListEmpty) {
            throw new InvalidStateException(INVALID_FEED_CREATE, new IllegalArgumentException("카테고리와 태그는 모두 입력되거나 모두 비워져야 합니다."));
        }

        // 태그가 있는 경우, 개수 최대 5개 제한
        if (!tagListEmpty && tagList.size() > 5) {
            throw new InvalidStateException(INVALID_FEED_CREATE, new IllegalArgumentException("태그는 최대 5개까지 입력할 수 있습니다."));
        }

        // 태그 중복 체크
        if (!tagListEmpty) {
            long distinctCount = tagList.stream().distinct().count();
            if (distinctCount != tagList.size()) {
                throw new InvalidStateException(INVALID_FEED_CREATE, new IllegalArgumentException("태그는 중복 될 수 없습니다."));
            }
        }
    }

    public static void validateImageCount(List<MultipartFile> images) {
        if (images != null && images.size() > 3) {
            throw new InvalidStateException(INVALID_FEED_CREATE, new IllegalArgumentException("이미지는 최대 3개까지 업로드할 수 있습니다."));
        }
    }

}
