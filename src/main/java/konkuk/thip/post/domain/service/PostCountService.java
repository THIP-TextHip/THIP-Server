package konkuk.thip.post.domain.service;

import konkuk.thip.common.annotation.application.DomainService;
import konkuk.thip.common.exception.InvalidStateException;

import static konkuk.thip.common.exception.code.ErrorCode.POST_LIKE_COUNT_UNDERFLOW;

@DomainService
public class PostCountService {

    public int updatePostLikeCount(boolean isLike, int likeCount) {
        if (isLike) {
            return ++likeCount;

        } else {
            checkLikeCountNotUnderflow(likeCount);
            return --likeCount;
        }
    }

    private void checkLikeCountNotUnderflow(int likeCount) {
        if (likeCount <= 0) {
            throw new InvalidStateException(POST_LIKE_COUNT_UNDERFLOW);
        }
    }
}
