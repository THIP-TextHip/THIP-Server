package konkuk.thip.recentSearch.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class RecentSearch extends BaseDomainEntity {

    private Long id;

    private String searchTerm;

    private String type;

    private Long userId;
}
