package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CountUpdatable;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
@SuperBuilder
public class Feed extends BaseDomainEntity implements CountUpdatable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return Objects.equals(id, feed.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Feed withoutId(String content, Long creatorId, Boolean isPublic, Long targetBookId,
                                 List<String> tagValues, List<String> imageUrls) {

        validateTags(tagValues);
        validateImageCount(imageUrls != null ? imageUrls.size() : 0);

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

    public void validateCreateComment(Long userId){
        if (!this.isPublic && !this.creatorId.equals(userId)) {
            throw new InvalidStateException(FEED_ACCESS_FORBIDDEN, new IllegalArgumentException("비공개 글은 작성자만 댓글을 쓸 수 있습니다."));
        }
    }

    public void validateLike(Long userId){
        if (!this.isPublic && !this.creatorId.equals(userId)) {
            throw new InvalidStateException(FEED_ACCESS_FORBIDDEN, new IllegalArgumentException("비공개 글은 작성자만 좋아요 할 수 있습니다."));
        }
    }

    private void validateCreator(Long userId) {
        if (!this.creatorId.equals(userId)) {
            throw new InvalidStateException(FEED_ACCESS_FORBIDDEN, new IllegalArgumentException("피드 작성자만 피드를 수정할 수 있습니다."));
        }
    }

    public void updateContent(Long userId, String newContent) {
        validateCreator(userId);
        this.content = newContent;
    }

    public void updateVisibility(Long userId, Boolean isPublic) {
        validateCreator(userId);
        this.isPublic = isPublic;
    }

    public void updateTags(Long userId, List<String> newTagValues) {
        validateCreator(userId);
        validateTags(newTagValues);
        this.tagList = Tag.fromList(newTagValues); // Tag.from(...) 등으로 변환
    }

    public void updateImages(Long userId, List<String> newImageUrls) {
        validateCreator(userId);
        validateImageCount(newImageUrls.size());
        validateOwnsImages(newImageUrls);

        this.contentList = convertToContentList(newImageUrls);
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

    @Override
    public void increaseCommentCount() {
        commentCount++;
    }

    @Override
    public void decreaseCommentCount() {
        checkCommentCountNotUnderflow();
        commentCount--;
    }

    @Override
    public void updateLikeCount(boolean like) {
        if (like) {
            likeCount++;
        } else {
            checkLikeCountNotUnderflow();
            likeCount--;
        }
    }

    private void checkCommentCountNotUnderflow() {
        if (commentCount <= 0) {
            throw new InvalidStateException(COMMENT_COUNT_UNDERFLOW);
        }
    }

    private void checkLikeCountNotUnderflow() {
        if (likeCount <= 0) {
            throw new InvalidStateException(POST_LIKE_COUNT_UNDERFLOW);
        }
    }

    /**
     * 유저가 현재 피드를 조회할 수 있는지를 검증하는 메서드
     */
    public void validateViewPermission(Long userId) {
        if (!isPublic && !creatorId.equals(userId)) {
            throw new BusinessException(FEED_CAN_NOT_SHOW_PRIVATE_ONE);
        }
    }
}
