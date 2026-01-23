package konkuk.thip.comment.adapter.out.mapper;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.comment.domain.Comment;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentJpaEntity toJpaEntity(Comment comment, PostJpaEntity postJpaEntity, UserJpaEntity userJpaEntity, CommentJpaEntity parentCommentJpaEntity, CommentJpaEntity rootCommentJpaEntity) {
        return CommentJpaEntity.builder()
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .reportCount(comment.getReportCount())
                .descendantCount(comment.getDescendantCount())
                .postJpaEntity(postJpaEntity)
                .postType(comment.getPostType())
                .userJpaEntity(userJpaEntity)
                .parent(parentCommentJpaEntity)
                .root(rootCommentJpaEntity)
                .build();
    }

    public Comment toDomainEntity(CommentJpaEntity commentJpaEntity) {
        return Comment.builder()
                .id(commentJpaEntity.getCommentId())
                .content(commentJpaEntity.getContent())
                .reportCount(commentJpaEntity.getReportCount())
                .likeCount(commentJpaEntity.getLikeCount())
                .descendantCount(commentJpaEntity.getDescendantCount())
                .targetPostId(commentJpaEntity.getPostJpaEntity().getPostId())
                .postType(commentJpaEntity.getPostType())
                .creatorId(commentJpaEntity.getUserJpaEntity().getUserId())
                .parentCommentId(commentJpaEntity.getParent() != null ? commentJpaEntity.getParent().getCommentId() : null)
                .rootCommentId(commentJpaEntity.getRoot() != null ? commentJpaEntity.getRoot().getCommentId() : null)
                .createdAt(commentJpaEntity.getCreatedAt())
                .modifiedAt(commentJpaEntity.getModifiedAt())
                .status(commentJpaEntity.getStatus())
                .build();
    }
}
