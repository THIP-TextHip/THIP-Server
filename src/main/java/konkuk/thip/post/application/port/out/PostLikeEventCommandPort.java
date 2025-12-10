package konkuk.thip.post.application.port.out;

import konkuk.thip.post.domain.PostType;

public interface PostLikeEventCommandPort {
    void publishEvent(
            Long userId, Long postId, boolean isLike, // true: 좋아요, false: 좋아요 취소
            PostType postType, int finalLikeCount // DB에 동기화되어야 할 최종 카운트
    );
}
