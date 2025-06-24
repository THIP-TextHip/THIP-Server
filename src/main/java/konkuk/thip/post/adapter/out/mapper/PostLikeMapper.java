package konkuk.thip.post.adapter.out.mapper;

import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import konkuk.thip.post.domain.PostLike;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PostLikeMapper {

    public PostLikeJpaEntity toJpaEntity(PostJpaEntity postJpaEntity, UserJpaEntity userJpaEntity) {
        return PostLikeJpaEntity.builder()
                .postJpaEntity(postJpaEntity)
                .userJpaEntity(userJpaEntity)
                .build();
    }

    public PostLike toDomainEntity(PostLikeJpaEntity postLikeJpaEntity) {
        return PostLike.builder()
                .id(postLikeJpaEntity.getLikeId())
                .targetPostId(postLikeJpaEntity.getPostJpaEntity().getPostId())
                .userId(postLikeJpaEntity.getUserJpaEntity().getUserId())
                .createdAt(postLikeJpaEntity.getCreatedAt())
                .modifiedAt(postLikeJpaEntity.getModifiedAt())
                .status(postLikeJpaEntity.getStatus())
                .build();
    }
}
