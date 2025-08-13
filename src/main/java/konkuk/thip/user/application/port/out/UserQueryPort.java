package konkuk.thip.user.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;

import java.util.List;
import java.util.Set;

public interface UserQueryPort {
    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);

    Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId);

    UserViewAliasChoiceResult getAllAliasesAndCategories();

    List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size);

    CursorBasedList<ReactionQueryDto> findLikeReactionsByUserId(Long userId, Cursor cursor, String label);

    CursorBasedList<ReactionQueryDto> findCommentReactionsByUserId(Long userId, Cursor cursor, String label);

    CursorBasedList<ReactionQueryDto> findBothReactionsByUserId(Long userId, Cursor cursor, String likeLabel, String commentLabel);

    List<UserQueryDto> findRecentFeedWritersOfMyFollowings(Long userId, int size);
}
