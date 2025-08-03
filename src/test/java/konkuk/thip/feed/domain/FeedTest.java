package konkuk.thip.feed.domain;

import konkuk.thip.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static konkuk.thip.common.exception.code.ErrorCode.FEED_CAN_NOT_SHOW_PRIVATE_ONE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class FeedTest {

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
}
