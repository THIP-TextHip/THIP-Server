package konkuk.thip.saved.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import konkuk.thip.saved.domain.SavedBook;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SavedBookMapper {

    public SavedBookJpaEntity toJpaEntity(UserJpaEntity userJpaEntity, BookJpaEntity bookJpaEntity) {
        return SavedBookJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .bookJpaEntity(bookJpaEntity)
                .build();
    }

    public SavedBook toDomainEntity(SavedBookJpaEntity savedBookJpaEntity) {
        return SavedBook.builder()
                .id(savedBookJpaEntity.getSavedId())
                .userId(savedBookJpaEntity.getUserJpaEntity().getUserId())
                .bookId(savedBookJpaEntity.getBookJpaEntity().getBookId())
                .createdAt(savedBookJpaEntity.getCreatedAt())
                .modifiedAt(savedBookJpaEntity.getModifiedAt())
                .status(savedBookJpaEntity.getStatus())
                .build();
    }
}
