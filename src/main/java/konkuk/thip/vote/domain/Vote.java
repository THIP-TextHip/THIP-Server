package konkuk.thip.vote.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.post.domain.service.PostCountService;
import konkuk.thip.room.domain.RoomPost;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
@SuperBuilder
public class Vote extends BaseDomainEntity implements CountUpdatable, RoomPost {

    private Long id;

    private String content;

    private Long creatorId;

    private Integer page;

    private boolean isOverview;

    @Builder.Default
    private Integer likeCount = 0;

    @Builder.Default
    private Integer commentCount = 0;

    private Long roomId;

    public static Vote withoutId(String content, Long creatorId, Integer page, boolean isOverview, Long roomId) {
        return Vote.builder()
                .id(null)
                .content(content)
                .creatorId(creatorId)
                .page(page)
                .isOverview(isOverview)
                .likeCount(0)
                .commentCount(0)
                .roomId(roomId)
                .build();
    }

    public void validateOverview(int totalPageCount) {
        double ratio = (double) page / totalPageCount;
        if (isOverview && ratio < 0.8) {
            String message = String.format(
                    "총평(isOverview)은 진행률이 80%% 이상일 때만 가능합니다. 현재 진행률 = %.2f%% (%d/%d)",
                    ratio * 100, page, totalPageCount
            );
            throw new InvalidStateException(VOTE_CANNOT_BE_OVERVIEW, new IllegalStateException(message));
        }
    }

    public void validatePage(int totalPageCount) {
        if (page < 1 || page > totalPageCount) {
            String message = String.format(
                    "페이지 범위가 잘못되었습니다. 현재 기록할 page = %d, 책 전체 page = %d",
                    page, totalPageCount
            );
            throw new InvalidStateException(INVALID_VOTE_PAGE_RANGE,
                    new IllegalArgumentException(message)
            );
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
    public void updateLikeCount(PostCountService postCountService, boolean isLike) {
        likeCount = postCountService.updatePostLikeCount(isLike, likeCount);
    }

    private void checkCommentCountNotUnderflow() {
        if (commentCount <= 0) {
            throw new InvalidStateException(COMMENT_COUNT_UNDERFLOW);
        }
    }

    private void validateCreator(Long userId) {
        if (!this.creatorId.equals(userId)) {
            throw new InvalidStateException(VOTE_ACCESS_FORBIDDEN, new IllegalArgumentException("투표 작성자만 투표를 수정/삭제할 수 있습니다."));
        }
    }

    public void validateDeletable(Long userId,Long roomId) {
        validateRoomId(roomId);
        validateCreator(userId);
    }

    private void validateRoomId(Long roomId) {
        if (!this.roomId.equals(roomId)) {
            throw new InvalidStateException(VOTE_ACCESS_FORBIDDEN, new IllegalArgumentException("투표가 해당 방에 속하지 않습니다."));
        }
    }
}
