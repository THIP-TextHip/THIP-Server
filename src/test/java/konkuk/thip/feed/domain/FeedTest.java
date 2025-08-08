package konkuk.thip.feed.domain;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.domain.service.PostCountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.feed.domain.Tag.BOOK_RECOMMEND;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] Feed 도메인 테스트")
class FeedTest {

    private PostCountService postCountService;

    @BeforeEach
     void setUp() {
        postCountService = new PostCountService();
    }

    private final Long CREATOR_ID = 1L;
    private final Long OTHER_USER_ID = 2L;

    private Feed createPublicFeed() {
        return Feed.builder()
                .id(100L)
                .creatorId(CREATOR_ID)
                .content("공개 피드 입니다.")
                .isPublic(true)
                .tagList(List.of(Tag.from(BOOK_RECOMMEND.getValue())))
                .contentList(List.of(Content.builder()
                                        .contentUrl("url1")
                                        .targetPostId(100L).build()

                ))
                .commentCount(1)
                .build();
    }

    private Feed createNotCommentFeed() {
        return Feed.builder()
                .id(101L)
                .creatorId(CREATOR_ID)
                .content("댓글이 없는 공개피드입니다.")
                .isPublic(true)
                .commentCount(0)
                .build();
    }

    private Feed createPrivateFeed() {
        return Feed.builder()
                .id(102L)
                .creatorId(CREATOR_ID)
                .content("비공개피드 입니다.")
                .isPublic(false)
                .commentCount(0)
                .build();
    }

    @Test
    @DisplayName("validateTags: 태그가 5개 초과 시 InvalidStateException이 발생한다.")
    void validateTags_exceedsMax_throws() {
        List<String> tags = List.of("a", "b", "c", "d", "e", "f");

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> Feed.validateTags(tags));

        assertEquals(INVALID_FEED_COMMAND,  ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("최대 5개"));
    }

    @Test
    @DisplayName("validateTags: 중복 태그 있을 경우 InvalidStateException이 발생한다.")
    void validateTags_withDuplicates_throws() {
        List<String> tags = List.of("a", "b", "a");

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> Feed.validateTags(tags));

        assertEquals(INVALID_FEED_COMMAND, ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("중복"));
    }


    @Test
    @DisplayName("validateImageCount: 3개 초과 이미지 업로드 시 InvalidStateException이 발생한다.")
    void validateImageCount_exceedsMax_throws() {
        int imageCount = 4;

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> Feed.validateImageCount(imageCount));

        assertEquals(INVALID_FEED_COMMAND,  ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("최대 3개"));
    }


    @Test
    @DisplayName("validateCreateComment: 공개 피드면 누구나 댓글을 작성 할 수 있다")
    void validateCreateComment_publicFeed_passes() {
        Feed feed = createPublicFeed();

        assertDoesNotThrow(() -> feed.validateCreateComment(OTHER_USER_ID));
    }

    @Test
    @DisplayName("validateCreateComment: 비공개 피드시에 작성자가 아닌 유저가 댓글을 작성하려고 하면 InvalidStateException이 발생한다.")
    void validateCreateComment_nonCreatorOnPrivateFeed_throws() {
        Feed feed = createPrivateFeed();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.validateCreateComment(OTHER_USER_ID));

        assertEquals(FEED_ACCESS_FORBIDDEN, ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("비공개 글은 작성자만"));
    }

    @Test
    @DisplayName("validateCreateComment: 비공개 피드시에 작성자만 댓글을 작성 할 수 있다.")
    void validateCreateComment_creatorOnPrivateFeed_passes() {
        Feed feed = createPrivateFeed();

        assertDoesNotThrow(() -> feed.validateCreateComment(CREATOR_ID));
    }


    @Test
    @DisplayName("updateContent: 작성자가 아닌 경우 피드 내용을 수정하려고 하면 InvalidStateException이 발생한다.")
    void updateContent_byNonCreator_throws() {
        Feed feed = createPublicFeed();
        String newContent = "새로운 피드 내용";

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.updateContent(OTHER_USER_ID, newContent));

        assertEquals(FEED_ACCESS_FORBIDDEN,ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("피드 작성자만"));
    }


    @Test
    @DisplayName("updateVisibility: 작성자가 아닌 경우 피드 공개여부를 수정하려고 하면 InvalidStateException이 발생한다.")
    void updateVisibility_byNonCreator_throws() {
        Feed feed = createPublicFeed();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.updateVisibility(OTHER_USER_ID, false));

        assertEquals(FEED_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateTags: 작성자가 아닌 경우 피드의 태그를 수정하려고 하면 InvalidStateException이 발생한다.")
    void updateTags_byNonCreator_throws(){
        Feed feed = createPublicFeed();
        List<String> tags = List.of(BOOK_RECOMMEND.getValue());

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.updateTags(OTHER_USER_ID, tags));

        assertEquals(FEED_ACCESS_FORBIDDEN, ex.getErrorCode());
    }


    @Test
    @DisplayName("updateImages: 작성자가 아닌 경우 피드의 이미지를 수정하려고 하면 InvalidStateException이 발생한다.")
    void updateImages_nonCreator_throws() {
        Feed feed = createPublicFeed();
        List<String> images = List.of("url1", "url2");

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.updateImages(OTHER_USER_ID, images));

        assertEquals(FEED_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateOwnsImages: 피드 수정 시에 존재하지 않는 이미지 URL 포함하여 수정하려고 하면 InvalidStateException이 발생한다.")
    void validateOwnsImages_withInvalidUrl_throws() {
        Feed feed = createPublicFeed();

        // feed.contentList에는 "url1"만 있음
        List<String> candidateImageUrls = List.of("url1", "invalidUrl");

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.validateOwnsImages(candidateImageUrls));

        assertEquals(INVALID_FEED_COMMAND, ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("해당 이미지는 이 피드에 존재하지 않습니다"));
    }


    @Test
    @DisplayName("increaseCommentCount: 피드의 댓글 수가 정상적으로 1 증가한다.")
    void increaseCommentCount_increments() {
        Feed feed = createPublicFeed();
        int before = feed.getCommentCount();

        feed.increaseCommentCount();

        assertEquals(before + 1, feed.getCommentCount());
    }

    @Test
    @DisplayName("decreaseCommentCount: 피드의 댓글 수가 정상적으로 1 감소한다.")
    void decreaseCommentCount_decrements() {
        Feed feed = createPublicFeed();
        int before = feed.getCommentCount();

        feed.decreaseCommentCount();

        assertEquals(before - 1, feed.getCommentCount());
    }

    @Test
    @DisplayName("decreaseCommentCount: 피드의 댓글 수가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void decreaseCommentCount_belowZero_throws() {
        Feed feed = createNotCommentFeed();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.decreaseCommentCount());

        assertEquals(COMMENT_COUNT_UNDERFLOW, ex.getErrorCode());
    }

    @Test
    @DisplayName("공개 피드는 누구나 조회 가능하다.")
    void validateViewPermission_no_exception_when_public_feed() throws Exception {
        //given
        Long userId = 100L;
        Feed publicFeed = makeFeedWithPublicStatus(true);

        //when //that
        assertDoesNotThrow(() -> publicFeed.validateViewPermission(userId));
    }

    @Test
    @DisplayName("피드 작성자는 비공개 피드도 조회 가능하다.")
    void validateViewPermission_no_exception_when_feed_owner_show_private_feed() throws Exception {
        //given
        Long feedOwnerId = 1L;
        Feed privateFeed = makeFeedWithPublicStatus(false);

        //when //that
        assertDoesNotThrow(() -> privateFeed.validateViewPermission(feedOwnerId));
    }

    @Test
    @DisplayName("피드 작성자가 아닌 다른 유저는 비공개 피드를 조회할 수 없다.")
    void validateViewPermission_exception_when_other_user_show_private_feed() throws Exception {
        //given
        Long otherUserId = 100L;
        Feed privateFeed = makeFeedWithPublicStatus(false);

        //when //that
        assertThatThrownBy(() -> privateFeed.validateViewPermission(otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(FEED_CAN_NOT_SHOW_PRIVATE_ONE.getMessage());
    }

    private Feed makeFeedWithPublicStatus(Boolean isPublic) {
        return Feed.builder()
                .id(1L)
                .content("테스트 내용")
                .creatorId(1L)
                .isPublic(isPublic)
                .targetBookId(100L)
                .tagList(Collections.emptyList())
                .contentList(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("updateLikeCount: like == true 면 likeCount 가 1씩 증가한다.")
    void updateLikeCount_likeTrue_increments() {
        Feed feed = createPublicFeed();

        feed.updateLikeCount(postCountService, true);
        assertEquals(1, feed.getLikeCount());

        feed.updateLikeCount(postCountService, true);
        assertEquals(2, feed.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 1씩 감소한다.")
    void updateLikeCount_likeFalse_decrements() {
        Feed feed = createPublicFeed();
        // 먼저 likeCount 증가 셋업
        feed.updateLikeCount(postCountService, true);
        feed.updateLikeCount(postCountService, true);
        assertEquals(2, feed.getLikeCount());

        feed.updateLikeCount(postCountService, false);
        assertEquals(1, feed.getLikeCount());

        feed.updateLikeCount(postCountService, false);
        assertEquals(0, feed.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void updateLikeCount_likeFalse_underflow_throws() {
        Feed feed = createPublicFeed();
        assertEquals(0, feed.getLikeCount());

        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> {
            feed.updateLikeCount(postCountService, false);
        });

        assertEquals(POST_LIKE_COUNT_UNDERFLOW, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateLike: 공개 피드는 누구나 좋아요 할 수 있다")
    void validateLike_publicFeed_anyUser_passes() {
        Feed feed = createPublicFeed();

        assertDoesNotThrow(() -> feed.validateLike(OTHER_USER_ID));
        assertDoesNotThrow(() -> feed.validateLike(CREATOR_ID));
    }

    @Test
    @DisplayName("validateLike: 비공개 피드이고 작성자가 아닌 경우 좋아요 시도하면  InvalidStateException이 발생한다.")
    void validateLike_privateFeed_nonCreator_throws() {
        Feed feed = createPrivateFeed();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.validateLike(OTHER_USER_ID));

        assertEquals(FEED_ACCESS_FORBIDDEN, ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("비공개 글은 작성자만 좋아요 할 수 있습니다."));
    }

    @Test
    @DisplayName("validateLike: 비공개 피드이고 작성자인 경우 좋아요 할 수 있다")
    void validateLike_privateFeed_creator_passes() {
        Feed feed = createPrivateFeed();

        assertDoesNotThrow(() -> feed.validateLike(CREATOR_ID));
    }

    @Test
    @DisplayName("validateDeletable: 작성자가 아닌 경우 피드를 삭제하려고 하면 InvalidStateException이 발생한다.")
    void validateDeletable_byNonCreator_throws(){
        Feed feed = createPublicFeed();
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> feed.validateDeletable(OTHER_USER_ID));

        assertEquals(FEED_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateDeletable: 피드의 작성자인 경우 피드를 삭제 할 수 있다.")
    void validateDeletable_byCreator_Success(){
        Feed feed = createPublicFeed();
        assertDoesNotThrow(() -> feed.validateCreateComment(CREATOR_ID));
    }

}
