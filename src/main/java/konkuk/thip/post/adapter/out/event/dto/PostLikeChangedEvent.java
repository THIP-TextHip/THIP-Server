package konkuk.thip.post.adapter.out.event.dto;

import konkuk.thip.post.domain.PostType;
import lombok.Builder;

@Builder
public record PostLikeChangedEvent(
        Long userId,
        Long postId,
        boolean isLike,         // true: 좋아요 요청, false: 취소 요청
        PostType postType,
        int finalLikeCount      // DB에 동기화되어야 할 최종 카운트
) {
}