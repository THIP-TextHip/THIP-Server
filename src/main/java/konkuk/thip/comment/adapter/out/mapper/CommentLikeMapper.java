package konkuk.thip.comment.adapter.out.mapper;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import konkuk.thip.comment.domain.CommentLike;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentLikeMapper {

    public CommentLikeJpaEntity toJpaEntity(UserJpaEntity userJpaEntity, CommentJpaEntity commentJpaEntity){
        return CommentLikeJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .commentJpaEntity(commentJpaEntity)
                .build();
    }

    public CommentLike toDomainEntity(CommentLikeJpaEntity commentLikeJpaEntity){
        return CommentLike.builder()
                .id(commentLikeJpaEntity.getLikeId())
                .userId(commentLikeJpaEntity.getUserJpaEntity().getUserId())
                .targetCommentId(commentLikeJpaEntity.getCommentJpaEntity().getCommentId())
                .createdAt(commentLikeJpaEntity.getCreatedAt())
                .modifiedAt(commentLikeJpaEntity.getModifiedAt())
                .status(commentLikeJpaEntity.getStatus())
                .build();
    }
}
