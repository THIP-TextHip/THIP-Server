package konkuk.thip.room.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PostLike extends BaseDomainEntity {

    private Long id;

    private Long targetPostId;

    private Long userId;
}
