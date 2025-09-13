package konkuk.thip.roompost.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.roompost.application.port.in.VoteDeleteUseCase;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteDeleteCommand;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.domain.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteDeleteService implements VoteDeleteUseCase {

    private final VoteCommandPort voteCommandPort;
    private final CommentCommandPort commentCommandPort;
    private final PostLikeCommandPort postLikeCommandPort;
    private final RoomCommandPort roomCommandPort;

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    @Transactional
    public Long deleteVote(VoteDeleteCommand command) {

        // 1. 방 참여자 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 1.1 방 존재 여부 및 만료 검증
        Room room = roomCommandPort.getByIdOrThrow(command.roomId());
        room.validateRoomInProgress();

        // 2. 투표 조회 및 검증
        Vote vote = voteCommandPort.getByIdOrThrow(command.voteId());
        // 2-1. 투표 삭제 권한 검증
        vote.validateDeletable(command.userId(),command.roomId());

        // 3. 투표 삭제
        // 3-1. 투표 게시글 댓글 삭제
        commentCommandPort.softDeleteAllByPostId(command.voteId());
        // 3-2. 투표 게시글 좋아요 삭제
        postLikeCommandPort.deleteAllByPostId(command.voteId());
        // 3-3. 투표 삭제
        voteCommandPort.delete(vote);

        return command.roomId();
    }
}
