package konkuk.thip.post.domain;

import konkuk.thip.post.domain.service.PostCountService;

public interface CountUpdatable {
    void increaseCommentCount();
    void decreaseCommentCount();
    void updateLikeCount(PostCountService postCountService, boolean isLike);
    Long getId();
}