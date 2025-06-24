package konkuk.thip.comment.adapter.out.mapper;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentJpaEntity toJpaEntity(Comment comment, PostJpaEntity postJpaEntity, UserJpaEntity userJpaEntity, CommentJpaEntity commentJpaEntity) {
        return CommentJpaEntity.builder()
                .content(comment.getContent())
                .reportCount(comment.getReportCount())
                .postJpaEntity(postJpaEntity)
                .userJpaEntity(userJpaEntity)
                .parent(commentJpaEntity)
                .build();
    }

    public Comment toDomainEntity(CommentJpaEntity commentJpaEntity) {
        return Comment.builder()
                .id(commentJpaEntity.getCommentId())
                .content(commentJpaEntity.getContent())
                .reportCount(commentJpaEntity.getReportCount())
                .targetPostId(commentJpaEntity.getPostJpaEntity().getPostId())
                .creatorId(commentJpaEntity.getUserJpaEntity().getUserId())
                .parentCommentId(commentJpaEntity.getParent() != null ? commentJpaEntity.getParent().getCommentId() : null)
                .createdAt(commentJpaEntity.getCreatedAt())
                .modifiedAt(commentJpaEntity.getModifiedAt())
                .status(commentJpaEntity.getStatus())
                .build();
    }
}
