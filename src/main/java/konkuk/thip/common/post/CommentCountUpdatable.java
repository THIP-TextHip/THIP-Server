package konkuk.thip.common.post;

public interface CommentCountUpdatable {
    void increaseCommentCount();
    void decreaseCommentCount();
    Long getId();
}