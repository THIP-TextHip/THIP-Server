package konkuk.thip.post.application.port.out;

import konkuk.thip.common.post.PostType;

public interface PostLikeCommandPort {
    void save(Long userId, Long postId, PostType postType);
    void delete(Long userId, Long postId);
}
