package konkuk.thip.record.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
@SuperBuilder
public class Record extends BaseDomainEntity {

    private Long id;

    private String content;

    private Long creatorId;

    private Integer page;

    private boolean isOverview;

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
            throw new InvalidStateException(RECORD_CANNOT_BE_OVERVIEW, new IllegalStateException(message));
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
}
