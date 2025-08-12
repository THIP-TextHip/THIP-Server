package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AttendanceCheck extends BaseDomainEntity {

    private Long id;

    private String todayComment;

    private Long roomId;

    private Long creatorId;

    public static AttendanceCheck withoutId(Long roomId, Long creatorId, String todayComment) {
        return AttendanceCheck.builder()
                .roomId(roomId)
                .creatorId(creatorId)
                .todayComment(todayComment)
                .build();
    }
}
