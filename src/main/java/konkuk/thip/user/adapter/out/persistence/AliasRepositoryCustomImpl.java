package konkuk.thip.user.adapter.out.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.room.adapter.out.jpa.QCategoryJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.application.port.in.dto.AliasChoiceViewResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AliasRepositoryCustomImpl implements AliasRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public AliasChoiceViewResult getAllAliasesAndCategories() {
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;
        QCategoryJpaEntity category = QCategoryJpaEntity.categoryJpaEntity;

        List<AliasChoiceViewResult.AliasChoice> aliasChoices = jpaQueryFactory
                .select(Projections.constructor(
                        AliasChoiceViewResult.AliasChoice.class,
                        alias.aliasId,
                        alias.value,
                        category.value,
                        alias.imageUrl,
                        alias.color
                ))
                .from(alias)
                .leftJoin(category)
                .on(category.aliasForCategoryJpaEntity.eq(alias))
                .fetch();

        return new AliasChoiceViewResult(aliasChoices);
    }
}
