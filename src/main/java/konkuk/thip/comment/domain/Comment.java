package konkuk.thip.comment.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Comment extends BaseDomainEntity {

    private Long id;

    private String content;

    private int reportCount;

    private Long targetPostId;

    private Long creatorId;

    private Long parentCommentId;

}
