package konkuk.thip.feed.adapter.out.mapper;

import konkuk.thip.book.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.room.adapter.out.jpa.PostJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagJpaEntity toJpaEntity(Tag tag, PostJpaEntity postJpaEntity, CategoryJpaEntity categoryJpaEntity) {
        return TagJpaEntity.builder()
                .value(tag.getValue())
                .postJpaEntity(postJpaEntity)
                .categoryJpaEntity(categoryJpaEntity)
                .build();
    }

    public Tag toDomainEntity(TagJpaEntity tagJpaEntity) {
        return Tag.builder()
                .id(tagJpaEntity.getTagId())
                .value(tagJpaEntity.getValue())
                .targetPostId(tagJpaEntity.getPostJpaEntity().getPostId())
                .categoryId(tagJpaEntity.getCategoryJpaEntity().getCategoryId())
                .createdAt(tagJpaEntity.getCreatedAt())
                .modifiedAt(tagJpaEntity.getModifiedAt())
                .status(tagJpaEntity.getStatus())
                .build();
    }
}
