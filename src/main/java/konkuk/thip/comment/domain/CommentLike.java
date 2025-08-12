package konkuk.thip.comment.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CommentLike extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long targetCommentId;
}
