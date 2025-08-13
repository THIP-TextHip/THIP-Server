package konkuk.thip.user.adapter.out.persistence.repository.alias;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.room.adapter.out.jpa.QCategoryJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AliasQueryRepositoryImpl implements AliasQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;
        QCategoryJpaEntity category = QCategoryJpaEntity.categoryJpaEntity;

        List<UserViewAliasChoiceResult.AliasChoice> aliasChoices = jpaQueryFactory
                .select(Projections.constructor(
                        UserViewAliasChoiceResult.AliasChoice.class,
                        // TODO : DB에 String alias 만 저장하도록 바뀐다면 쿼리 수정해야함
                        alias.value,
                        category.value,
                        alias.imageUrl,
                        alias.color
                ))
                .from(alias)
                .leftJoin(category)
                .on(category.aliasForCategoryJpaEntity.eq(alias))
                .orderBy(alias.aliasId.asc())
                .fetch();

        return new UserViewAliasChoiceResult(aliasChoices);
    }
}
