package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.adapter.out.persistence.function.ReactionQueryFunction;
import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserQueryPersistenceAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final AliasJpaRepository aliasJpaRepository;

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByNicknameAndUserIdNot(String nickname, Long userId) {
        return userJpaRepository.existsByNicknameAndUserIdNot(nickname, userId);
    }

    @Override
    public boolean existsByOauth2Id(String oauth2Id) {
        return userJpaRepository.existsByOauth2Id(oauth2Id);
    }

    @Override
    public Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId) {
        return  userJpaRepository.findUserIdsByBookId(bookId);
    }

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        return aliasJpaRepository.getAllAliasesAndCategories();
    }

    @Override
    public List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size) {
        return userJpaRepository.findUsersByNicknameOrderByAccuracy(keyword, userId, size);
    }

    @Override
    public CursorBasedList<ReactionQueryDto> findLikeReactionsByUserId(Long userId, Cursor cursor, String likeLabel) {
        return getReactions(userId, cursor,
                (id, cursorDateTime, size) -> userJpaRepository.findLikeByUserId(id, cursorDateTime, size, likeLabel)
        );
    }

    @Override
    public CursorBasedList<ReactionQueryDto> findCommentReactionsByUserId(Long userId, Cursor cursor, String commentLabel) {
        return getReactions(userId, cursor,
                (id, cursorDateTime, size) -> userJpaRepository.findCommentByUserId(id, cursorDateTime, size, commentLabel)
        );
    }

    @Override
    public CursorBasedList<ReactionQueryDto> findBothReactionsByUserId(Long userId, Cursor cursor, String likeLabel, String commentLabel) {
        return getReactions(userId, cursor,
                (id, cursorDateTime, size) -> userJpaRepository.findLikeAndCommentByUserId(id, cursorDateTime, size, likeLabel, commentLabel)
        );
    }

    @Override
    public List<UserQueryDto> findRecentFeedWritersOfMyFollowings(Long userId, int size) {
        return userJpaRepository.findFeedWritersOfMyFollowingsOrderByCreatedAtDesc(userId, size);
    }

    private CursorBasedList<ReactionQueryDto> getReactions(Long userId, Cursor cursor, ReactionQueryFunction reactionQueryFunction) {
        LocalDateTime cursorLocalDateTime = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);

        List<ReactionQueryDto> reactionQueryDtos = reactionQueryFunction.fetch(userId, cursorLocalDateTime, cursor.getPageSize());

        return CursorBasedList.of(reactionQueryDtos, cursor.getPageSize(), reactionQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(reactionQueryDto.createdAt().toString()));
            return nextCursor.toEncodedString();
        });
    }
}