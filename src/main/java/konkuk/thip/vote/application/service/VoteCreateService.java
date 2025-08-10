package konkuk.thip.vote.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.vote.application.port.in.VoteCreateUseCase;
import konkuk.thip.vote.application.port.in.dto.VoteCreateCommand;
import konkuk.thip.vote.application.port.in.dto.VoteCreateResult;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;
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

        Room room = roomCommandPort.getByIdOrThrow(vote.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());
        validateVote(vote, room, book);

        // 2. vote 저장
        Long savedVoteId = voteCommandPort.saveVote(vote);

        // 3. vote item 저장
        List<VoteItem> voteItems = command.voteItemCreateCommands().stream()
                .map(itemCmd -> VoteItem.withoutId(
                        itemCmd.itemName(),
                        0,
                        savedVoteId
                ))
                .toList();
        voteCommandPort.saveAllVoteItems(voteItems);

        // 4. RoomParticipant 정보 update
        updateRoomProgress(vote, room, book);

        return VoteCreateResult.of(savedVoteId, command.roomId());
    }

    private void updateRoomProgress(Vote vote, Room room, Book book) {
        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(vote.getCreatorId(), vote.getRoomId());

        if(roomParticipant.updateUserProgress(vote.getPage(), book.getPageCount())) {
            // userPercentage가 업데이트되었으면 Room의 roomPercentage 업데이트
            List<RoomParticipant> roomParticipantList = roomParticipantCommandPort.findAllByRoomId(vote.getRoomId());
            Double totalUserPercentage = roomParticipantList.stream()
                    .filter(participant -> !roomParticipant.getId().equals(participant.getId())) // 현재 업데이트 중인 사용자 제외
                    .map(RoomParticipant::getUserPercentage)
                    .reduce(0.0, Double::sum);
            totalUserPercentage += roomParticipant.getUserPercentage();
            room.updateRoomPercentage(totalUserPercentage / roomParticipantList.size());
        }

        roomCommandPort.update(room);
        roomParticipantCommandPort.update(roomParticipant);
    }

    private void validateVote(Vote vote, Room room, Book book) {
        // 페이지 유효성 검증
        vote.validatePage(book.getPageCount());

        // 총평 유효성 검증
        vote.validateOverview(book.getPageCount());
    }
}
