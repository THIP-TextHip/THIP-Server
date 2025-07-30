package konkuk.thip.recentSearch.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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

    public void updateModifiedAt(LocalDateTime localDateTime) {
        this.setModifiedAt(localDateTime);
    }
}
