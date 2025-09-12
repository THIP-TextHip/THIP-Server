package konkuk.thip.roompost.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.domain.service.PostCountService;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
@SuperBuilder
public class Record extends BaseDomainEntity implements RoomPost {

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

    public static Record withoutId(
            String content,
            Long creatorId,
            Integer page,
            boolean isOverview,
            Long roomId
    ) {
        return Record.builder()
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
        // 총평 기록 생성 요청인데 page가 책의 전체 페이지 수가 아니라면 에러
        if (isOverview && page != totalPageCount) {
            String message = String.format(
                    "총평(isOverview)은 책의 전체 페이지 수(%d)와 동일한 페이지에서만 작성할 수 있습니다. 현재 페이지 = %d",
                    totalPageCount, page
            );
            throw new InvalidStateException(RECORD_CANNOT_BE_OVERVIEW, new IllegalArgumentException(message));
        }
    }

    public void validatePage(int totalPageCount) {
        if (page < 1 || page > totalPageCount) {
            String message = String.format(
                    "페이지 범위가 잘못되었습니다. 현재 기록할 page = %d, 책 전체 page = %d",
                    page, totalPageCount
            );
            throw new InvalidStateException(INVALID_RECORD_PAGE_RANGE,
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
            throw new InvalidStateException(RECORD_ACCESS_FORBIDDEN, new IllegalArgumentException("기록 작성자만 기록을 수정/삭제/핀 할 수 있습니다."));
        }
    }

    public void updateRecord(Long userId, Long roomId, String content) {
        validateRoomId(roomId);
        validateCreator(userId);
        this.content = content;
    }

    public void validateDeletable(Long userId,Long roomId) {
        validateRoomId(roomId);
        validateCreator(userId);
    }

    private void validateRoomId(Long roomId) {
        if (!this.roomId.equals(roomId)) {
            throw new InvalidStateException(RECORD_ACCESS_FORBIDDEN, new IllegalArgumentException("기록이 해당 방에 속하지 않습니다."));
        }
    }

    public void validatePin(Long userId,Long roomId) {
        validateRoomId(roomId);
        validateCreator(userId);
    }

}