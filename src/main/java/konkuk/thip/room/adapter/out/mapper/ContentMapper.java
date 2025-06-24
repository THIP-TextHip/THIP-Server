package konkuk.thip.room.adapter.out.mapper;

import konkuk.thip.room.adapter.out.jpa.ContentJpaEntity;
import konkuk.thip.room.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.room.domain.Content;
import org.springframework.stereotype.Component;

@Component
public class ContentMapper {

    public ContentJpaEntity toJpaEntity(Content content, PostJpaEntity postJpaEntity) {
        return ContentJpaEntity.builder()
                .contentUrl(content.getContentUrl())
                .postJpaEntity(postJpaEntity)
                .build();
    }

    public Content toDomainEntity(ContentJpaEntity contentJpaEntity) {
        return Content.builder()
                .id(contentJpaEntity.getContentId())
                .contentUrl(contentJpaEntity.getContentUrl())
                .targetPostId(contentJpaEntity.getPostJpaEntity().getPostId())
                .createdAt(contentJpaEntity.getCreatedAt())
                .modifiedAt(contentJpaEntity.getModifiedAt())
                .status(contentJpaEntity.getStatus())
                .build();
    }
}
