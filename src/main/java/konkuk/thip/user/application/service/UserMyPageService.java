package konkuk.thip.user.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.adapter.in.web.response.UserProfileResponse;
import konkuk.thip.user.adapter.in.web.response.UserReactionResponse;
import konkuk.thip.user.application.mapper.ReactionQueryMapper;
import konkuk.thip.user.application.port.in.UserMyPageUseCase;
import konkuk.thip.user.application.port.in.dto.UserReactionType;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMyPageService implements UserMyPageUseCase {

    private final UserQueryPort userQueryPort;
    private final UserCommandPort userCommandPort;

    private final ReactionQueryMapper reactionQueryMapper;

    @Override
    public UserReactionResponse getUserReaction(Long userId, UserReactionType userReactionType, int size, String cursorStr) {

        Cursor cursor = Cursor.from(cursorStr, size);

        CursorBasedList<ReactionQueryDto> reactionQueryDtoList = switch (userReactionType) {
            case LIKE -> userQueryPort.findLikeReactionsByUserId(userId, cursor);
            case COMMENT -> userQueryPort.findCommentReactionsByUserId(userId, cursor);
            case BOTH -> userQueryPort.findBothReactionsByUserId(userId, cursor);
        };

        List<UserReactionResponse.ReactionDto> reactionDtoList = reactionQueryMapper.toReactionDtoList(reactionQueryDtoList.contents());
        return UserReactionResponse.of(
                reactionDtoList,
                reactionQueryDtoList.nextCursor(),
                reactionQueryDtoList.isLast()
        );
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userCommandPort.findById(userId);

        return UserProfileResponse.of(user.getAlias().getImageUrl(), user.getNickname(), user.getAlias().getValue());
    }
}
