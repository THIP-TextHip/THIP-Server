package konkuk.thip.feed.adapter.out.mapper;

import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.feed.domain.TagName;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagJpaEntity toJpaEntity(Tag tag, PostJpaEntity postJpaEntity, CategoryJpaEntity categoryJpaEntity) {
        return TagJpaEntity.builder()
                .value(tag.getValue().getTag())
                .postJpaEntity(postJpaEntity)
                .categoryJpaEntity(categoryJpaEntity)
                .build();
    }

    public Tag toDomainEntity(TagJpaEntity tagJpaEntity) {
        return Tag.builder()
                .value(TagName.from(tagJpaEntity.getValue()))
                .build();
    }
}
