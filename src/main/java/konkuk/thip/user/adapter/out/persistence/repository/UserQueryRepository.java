package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface UserQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);

    List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size);

    List<ReactionQueryDto> findLikeByUserId(Long userId, LocalDateTime cursorLocalDateTime, Integer size, String likeLabel);

    List<ReactionQueryDto> findCommentByUserId(Long userId, LocalDateTime cursorLocalDateTime, Integer size, String commentLabel);

    List<ReactionQueryDto> findLikeAndCommentByUserId(Long userId, LocalDateTime cursorLocalDateTime, Integer size, String likeLabel, String commentLabel);

    List<UserQueryDto> findFeedWritersOfMyFollowingsOrderByCreatedAtDesc(Long userId, Integer size);
}
