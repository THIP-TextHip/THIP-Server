package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.mapper.CategoryMapper;
import konkuk.thip.room.application.port.out.CategoryCommandPort;
import konkuk.thip.room.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.CATEGORY_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class CategoryCommandPersistenceAdapter implements CategoryCommandPort {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Category findByValue(String value) {
        CategoryJpaEntity categoryJpaEntity = categoryJpaRepository.findByValue(value).orElseThrow(
                () -> new EntityNotFoundException(CATEGORY_NOT_FOUND)
        );

        return categoryMapper.toDomainEntity(categoryJpaEntity);
    }
}
