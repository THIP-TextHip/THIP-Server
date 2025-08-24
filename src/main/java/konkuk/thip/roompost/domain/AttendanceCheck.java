package konkuk.thip.roompost.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.ATTENDANCE_CHECK_CAN_NOT_DELETE;
import static konkuk.thip.common.exception.code.ErrorCode.ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED;

@Getter
@SuperBuilder
public class AttendanceCheck extends BaseDomainEntity {

    private static final int LIMIT_WRITE_COUNT_PER_DAY = 5;

    private Long id;

    private String todayComment;

    private Long roomId;

    private Long creatorId;

    public static AttendanceCheck withoutId(Long roomId, Long creatorId, String todayComment, int alreadyWrittenCountTodayOfUser) {
        validateWriteCount(alreadyWrittenCountTodayOfUser);

        return AttendanceCheck.builder()
                .roomId(roomId)
                .creatorId(creatorId)
                .todayComment(todayComment)
                .build();
    }

    private static void validateWriteCount(int alreadyWrittenCountTodayOfUser) {
        if (alreadyWrittenCountTodayOfUser >= LIMIT_WRITE_COUNT_PER_DAY) {
            throw new InvalidStateException(ATTENDANCE_CHECK_WRITE_LIMIT_EXCEEDED);
        }
    }

    public void validateCreator(Long userId) {
        if (!creatorId.equals(userId)) {
            throw new InvalidStateException(ATTENDANCE_CHECK_CAN_NOT_DELETE);
        }
    }
}
