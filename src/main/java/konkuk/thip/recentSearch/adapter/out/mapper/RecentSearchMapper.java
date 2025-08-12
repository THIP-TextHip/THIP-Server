package konkuk.thip.recentSearch.adapter.out.mapper;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.domain.RecentSearch;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecentSearchMapper {

    public RecentSearchJpaEntity toJpaEntity(RecentSearch recentSearch, UserJpaEntity userJpaEntity) {
        return RecentSearchJpaEntity.builder()
                .searchTerm(recentSearch.getSearchTerm())
                .type(recentSearch.getType())
                .userJpaEntity(userJpaEntity)
                .build();
    }

    public RecentSearch toDomainEntity(RecentSearchJpaEntity recentSearchJpaEntity) {
        return RecentSearch.builder()
                .id(recentSearchJpaEntity.getRecentSearchId())
                .searchTerm(recentSearchJpaEntity.getSearchTerm())
                .type(recentSearchJpaEntity.getType())
                .userId(recentSearchJpaEntity.getUserJpaEntity().getUserId())
                .createdAt(recentSearchJpaEntity.getCreatedAt())
                .modifiedAt(recentSearchJpaEntity.getModifiedAt())
                .status(recentSearchJpaEntity.getStatus())
                .build();
    }
}
