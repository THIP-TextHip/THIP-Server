package konkuk.thip.recentSearch.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.recentSearch.domain.value.RecentSearchType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class RecentSearch extends BaseDomainEntity {

    private Long id;

    private String searchTerm;

    private RecentSearchType type;

    private Long userId;

    public static RecentSearch withoutId(String searchTerm, RecentSearchType type, Long userId) {
        return RecentSearch.builder()
                .searchTerm(searchTerm)
                .type(type)
                .userId(userId)
                .build();
    }
}
