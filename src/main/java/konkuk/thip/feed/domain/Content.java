package konkuk.thip.feed.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Content extends BaseDomainEntity {

    private Long id;

    private String contentUrl;

    private Long targetPostId;
}
