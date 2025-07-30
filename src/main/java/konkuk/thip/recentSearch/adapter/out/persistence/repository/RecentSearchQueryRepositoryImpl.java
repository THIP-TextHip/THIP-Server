package konkuk.thip.recentSearch.adapter.out.persistence.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.recentSearch.adapter.out.jpa.QRecentSearchJpaEntity.recentSearchJpaEntity;

@Repository
@RequiredArgsConstructor
public class RecentSearchQueryRepositoryImpl implements RecentSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RecentSearchJpaEntity> findBySearchTermAndTypeAndUserId(String searchTerm, SearchType type, Long userId) {
        RecentSearchJpaEntity result = queryFactory
                .selectFrom(recentSearchJpaEntity)
                .where(
                        recentSearchJpaEntity.searchTerm.eq(searchTerm),
                        recentSearchJpaEntity.type.eq(type),
                        recentSearchJpaEntity.userJpaEntity.userId.eq(userId)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
