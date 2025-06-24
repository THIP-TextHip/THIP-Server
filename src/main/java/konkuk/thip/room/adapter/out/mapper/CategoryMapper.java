package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.domain.Category;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryJpaEntity toJpaEntity(Category category, AliasJpaEntity aliasJpaEntity) {
        return CategoryJpaEntity.builder()
                .value(category.getValue())
                .aliasForCategoryJpaEntity(aliasJpaEntity)
                .build();
    }

    public Category toDomainEntity(CategoryJpaEntity categoryJpaEntity) {
        return Category.builder()
                .id(categoryJpaEntity.getCategoryId())
                .value(categoryJpaEntity.getValue())
                .aliasId(categoryJpaEntity.getAliasForCategoryJpaEntity().getAliasId())
                .createdAt(categoryJpaEntity.getCreatedAt())
                .modifiedAt(categoryJpaEntity.getModifiedAt())
                .status(categoryJpaEntity.getStatus())
                .build();
    }
}
