package konkuk.thip.roompost.application.service;

import konkuk.thip.comment.application.port.out.CommentCommandPort;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.roompost.application.port.in.RecordDeleteUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordDeleteCommand;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.domain.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordDeleteService implements RecordDeleteUseCase {

    private final RecordCommandPort recordCommandPort;
    private final CommentCommandPort commentCommandPort;
    private final PostLikeCommandPort postLikeCommandPort;
    private final RoomCommandPort roomCommandPort;

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    @Transactional
    public Long deleteRecord(RecordDeleteCommand command) {

        // 1. 방 참여자 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        Room room = roomCommandPort.getByIdOrThrow(command.roomId());
        room.validateRoomExpired();

        // 2. 기록 조회 및 검증
        Record record = recordCommandPort.getByIdOrThrow(command.recordId());
        // 2-1. 기록 삭제 권한 검증
        record.validateDeletable(command.userId(),command.roomId());

        // 3. 기록 삭제
        // 3-1. 기록 게시물 댓글 삭제
        commentCommandPort.softDeleteAllByPostId(command.recordId());
        // 3-2. 피드 게시물 좋아요 삭제
        postLikeCommandPort.deleteAllByPostId(command.recordId());
        // 3-3. 기록 삭제
        recordCommandPort.delete(record);

        return command.roomId();
    }
}
