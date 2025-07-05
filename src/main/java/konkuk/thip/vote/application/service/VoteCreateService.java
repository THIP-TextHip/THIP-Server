package konkuk.thip.vote.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.vote.application.port.in.VoteCreateUseCase;
import konkuk.thip.vote.application.port.in.dto.VoteCreateCommand;
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

    private final VoteCommandPort voteCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;

    @Transactional
    @Override
    public Long createVote(VoteCreateCommand command) {
        // 1. validate
        Vote vote = Vote.withoutId(
                command.content(),
                command.userId(),
                command.page(),
                command.isOverview(),
                command.roomId()
        );

        validateVote(vote);

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

        return savedVoteId;
    }

    private void validateVote(Vote vote) {
        Room room = roomCommandPort.findById(vote.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());

        // 페이지 유효성 검증
        vote.validatePage(book.getPageCount());

        // 총평 유효성 검증
        vote.validateOverview(book.getPageCount());
    }
}
