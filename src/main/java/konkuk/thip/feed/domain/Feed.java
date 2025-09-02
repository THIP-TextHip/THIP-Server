package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.feed.domain.value.ContentList;
import konkuk.thip.feed.domain.value.Tag;
import konkuk.thip.feed.domain.value.TagList;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.domain.service.PostCountService;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

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

    @Builder.Default
    private TagList tagList = TagList.empty();

    @Builder.Default
    private ContentList contentList = ContentList.empty();

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

        return Feed.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .isPublic(isPublic)
                .reportCount(0)
                .likeCount(0)
                .commentCount(0)
                .targetBookId(targetBookId)
                .tagList(convertToTagList(tagValues))
                .contentList(convertToContentList(imageUrls))
                .build();
    }

    private static TagList convertToTagList(List<String> tagValues) {
        if (tagValues == null || tagValues.isEmpty()) {
            return TagList.empty();
        }
        List<Tag> tags = Tag.fromList(tagValues);
        return TagList.of(tags);
    }

    private static ContentList convertToContentList(List<String> imageUrls) {
        if (imageUrls == null) return ContentList.empty();
        return ContentList.of(imageUrls);
    }


    // 공통된 비공개 접근 권한 검증 로직
    private void validatePrivateAccessPermission(Long userId, String action) {
        if (!this.isPublic && !this.creatorId.equals(userId)) {
            throw new InvalidStateException(FEED_ACCESS_FORBIDDEN,
                    new IllegalArgumentException(String.format("비공개 글은 작성자만 %s 있습니다.", action)));
        }
    }

    // 댓글 작성 권한 검증
    public void validateCreateComment(Long userId){
        validatePrivateAccessPermission(userId, "댓글을 쓸 수");
    }

    // 좋아요 권한 검증
    public void validateLike(Long userId){
        validatePrivateAccessPermission(userId, "좋아요 할 수");
    }

    private void validateCreator(Long userId) {
        if (!this.creatorId.equals(userId)) {
            throw new InvalidStateException(FEED_ACCESS_FORBIDDEN, new IllegalArgumentException("피드 작성자만 피드를 수정/삭제할 수 있습니다."));
        }
    }

    public void validateDeletable(Long userId) {
        validateCreator(userId);
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
        List<Tag> tags = Tag.fromList(newTagValues);
        this.tagList = TagList.of(tags);
    }

    public void updateImages(Long userId, List<String> newImageUrls) {
        validateCreator(userId);
//        validateImageCount(newImageUrls.size());
//        validateOwnsImages(newImageUrls);
        contentList.validateOwnImages(newImageUrls);

        this.contentList = convertToContentList(newImageUrls);
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
    public void updateLikeCount(PostCountService postCountService, boolean isLike) {
        likeCount = postCountService.updatePostLikeCount(isLike, likeCount);
    }

    private void checkCommentCountNotUnderflow() {
        if (commentCount <= 0) {
            throw new InvalidStateException(COMMENT_COUNT_UNDERFLOW);
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
