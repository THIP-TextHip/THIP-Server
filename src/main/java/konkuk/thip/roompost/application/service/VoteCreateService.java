package konkuk.thip.roompost.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.roompost.application.port.in.VoteCreateUseCase;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateResult;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.application.service.manager.RoomProgressManager;
import konkuk.thip.roompost.domain.Vote;
import konkuk.thip.roompost.domain.VoteItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteCreateService implements VoteCreateUseCase {

    private final RoomParticipantValidator roomParticipantValidator;
    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final VoteCommandPort voteCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;

    private final RoomProgressManager roomProgressManager;

    private final RoomNotificationOrchestrator roomNotificationOrchestrator;

    @Transactional
    @Override
    public VoteCreateResult createVote(VoteCreateCommand command) {
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 1. validate
        Vote vote = Vote.withoutId(
                command.content(),
                command.userId(),
                command.page(),
                command.isOverview(),
                command.roomId()
        );

        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(command.userId(), command.roomId());
        Room room = roomCommandPort.getByIdOrThrow(vote.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());
        validateVote(vote, book);

        // 2. vote 저장
        Long savedVoteId = voteCommandPort.saveVote(vote);

        // 3. vote item 저장
        List<VoteItem> voteItems = command.voteItemCreateCommands().stream()
                .map(itemCmd -> VoteItem.withoutId(
                        itemCmd.itemName(),
                        savedVoteId
                ))
                .toList();
        voteCommandPort.saveAllVoteItems(voteItems);

        // 4. RoomParticipant, Room progress 정보 update
        roomProgressManager.updateUserAndRoomProgress(roomParticipant, room, book, vote.getPage());

        // 5. 모임방 참여자들에게 투표 생성 알림 전송 (본인 제외)
        sendNotifications(command, room, vote, savedVoteId);

        return VoteCreateResult.of(savedVoteId, command.roomId());
    }

    private void sendNotifications(VoteCreateCommand command, Room room, Vote vote, Long newVoteId) {
        List<RoomParticipant> targetUsers = roomParticipantCommandPort.findAllByRoomId(command.roomId());
        for (RoomParticipant targetUser : targetUsers) {
            if (targetUser.getUserId().equals(command.userId())) continue; // 본인 제외
            roomNotificationOrchestrator.notifyRoomVoteStarted(targetUser.getUserId(), room.getId(), room.getTitle(), vote.getPage(), newVoteId);
        }
    }

    private void validateVote(Vote vote, Book book) {
        // 페이지 유효성 검증
        vote.validatePage(book.getPageCount());

        // 총평 유효성 검증
        vote.validateOverview(book.getPageCount());
    }
}
