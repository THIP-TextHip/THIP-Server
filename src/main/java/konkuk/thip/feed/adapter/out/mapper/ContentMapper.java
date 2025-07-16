package konkuk.thip.feed.adapter.out.mapper;

import konkuk.thip.feed.adapter.out.jpa.ContentJpaEntity;
import konkuk.thip.feed.domain.Content;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
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
