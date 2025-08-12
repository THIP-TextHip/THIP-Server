package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserReactionType;
import konkuk.thip.user.adapter.in.web.response.UserProfileResponse;
import konkuk.thip.user.adapter.in.web.response.UserReactionResponse;

public interface UserMyPageUseCase {

    /**
     * 사용자 반응 조회
     */
    UserReactionResponse getUserReaction(Long userId, UserReactionType userReactionType,
                                         int size, String cursor);

    /**
     * 사용자 마이페이지 조회
     */
    UserProfileResponse getUserProfile(Long userId);
}
