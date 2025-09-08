package konkuk.thip.user.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.comment.application.port.out.CommentLikeCommandPort;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.roompost.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.user.application.port.UserTokenBlacklistCommandPort;
import konkuk.thip.user.application.port.in.UserDeleteUseCase;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.USER_CANNOT_DELETE_ROOM_HOST;

@Service
@RequiredArgsConstructor
public class UserDeleteService implements UserDeleteUseCase {

    private final UserCommandPort userCommandPort;
    private final FollowingCommandPort followingCommandPort;
    private final FeedCommandPort feedCommandPort;
    private final BookCommandPort bookCommandPort;
    private final VoteCommandPort voteCommandPort;
    private final CommentCommandPort commentCommandPort;
    private final PostLikeCommandPort postLikeCommandPort;
    private final RecordCommandPort recordCommandPort;
    private final CommentLikeCommandPort commentLikeCommandPort;
    private final RecentSearchCommandPort recentSearchCommandPort;
    private final AttendanceCheckCommandPort attendanceCheckCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final UserTokenBlacklistCommandPort userTokenBlacklistCommandPort;

    @Override
    @Transactional
    public void deleteUser(Long userId, String authToken)  {

        // 1. 진행/모집 중인 방의 호스트일경우 회원탈퇴 불가
        boolean isHostInActiveRoom = roomParticipantCommandPort.existsHostUserInActiveRoom(userId);
        if (isHostInActiveRoom) {
            throw new BusinessException(USER_CANNOT_DELETE_ROOM_HOST);
        }

        // 2. 유저 조회 및 검증
        User user = userCommandPort.findById(userId);
        user.markAsDeleted();

        // 3. 유저가 남긴 관련 정보들 삭제
        // 팔로잉 관계 삭제
        followingCommandPort.deleteAllByUserId(userId);
        // 최근검색어 삭제
        recentSearchCommandPort.deleteAllByUserId(userId);
        // 알림 삭제 // TODO 알림구현 적용되면 수정
        // notificationCommandPort.softDeleteAllByUserId(userId);
        // 책/피드 저장 관계 삭제
        feedCommandPort.deleteAllSavedFeedByUserId(userId);
        bookCommandPort.deleteAllSavedBookByUserId(userId);
        // 오늘의 한마디 관계 삭제
        attendanceCheckCommandPort.deleteAllByUserId(userId);
        // 투표 참여 관계 삭제 -> 투표한 항목의 득표 수 감소
        voteCommandPort.deleteAllVoteParticipantByUserId(userId);
        // 댓글 좋아요 삭제 -> 댓글의 좋아요 수 감소
        commentLikeCommandPort.deleteAllByUserId(userId);
        // 댓글 삭제 -> 게시글의 댓글 수 감소, 댓글의 좋아요 삭제
        commentCommandPort.deleteAllByUserId(userId);
        // 게시글 좋아요 삭제 -> 게시글의 좋아요 수 감소
        postLikeCommandPort.deleteAllByUserId(userId);

        // 피드 삭제
        feedCommandPort.deleteAllFeedByUserId(userId);
        // 기록 삭제
        recordCommandPort.deleteAllByUserId(userId);
        // 투표 삭제
        voteCommandPort.deleteAllVoteByUserId(userId);

        // 방 참여 관계 삭제
        roomParticipantCommandPort.deleteAllByUserId(userId);
        // 유저 삭제
        userCommandPort.delete(user);
        // 토큰 블랙리스트 추가
        userTokenBlacklistCommandPort.addTokenToBlacklist(authToken);
    }

}
