package konkuk.thip.recentSearch.adapter.out.persistence.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.entity.StatusType.ACTIVE;
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
                        recentSearchJpaEntity.userJpaEntity.userId.eq(userId),
                        recentSearchJpaEntity.status.eq(ACTIVE)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<RecentSearchJpaEntity> findByTypeAndUserId(String type, Long userId) {
        return queryFactory
                .selectFrom(recentSearchJpaEntity)
                .where(
                        recentSearchJpaEntity.type.eq(SearchType.from(type)),
                        recentSearchJpaEntity.userJpaEntity.userId.eq(userId),
                        recentSearchJpaEntity.status.eq(ACTIVE)
                )
                .orderBy(recentSearchJpaEntity.modifiedAt.desc())
                .limit(5)
                .fetch();
    }
}
