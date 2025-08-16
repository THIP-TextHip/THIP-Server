package konkuk.thip.room.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.room.application.port.in.RoomShowPlayingDetailViewUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.RoomParticipants;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.roompost.application.port.out.VoteQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomShowPlayingDetailViewService implements RoomShowPlayingDetailViewUseCase {

    private static final int TOP_PARTICIPATION_VOTES_COUNT = 3;

    private final RoomCommandPort roomCommandPort;
    private final RoomQueryPort roomQueryPort;
    private final BookCommandPort bookCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final VoteQueryPort voteQueryPort;

    @Override
    @Transactional(readOnly = true)
    public RoomPlayingDetailViewResponse getPlayingRoomDetailView(Long userId, Long roomId) {
        // 1. Room 조회, Book 조회, Category와 연관된 Alias 조회
        Room room = roomCommandPort.getByIdOrThrow(roomId);
        Book book = bookCommandPort.findById(room.getBookId());

        // 2. Room과 연관된 UserRoom 조회, RoomParticipants 일급 컬렉션 생성
        // TODO. Room 도메인에 memberCount 값 추가된 후 리펙토링
        List<RoomParticipant> findByRoomId = roomParticipantCommandPort.findAllByRoomId(roomId);
        RoomParticipants roomParticipants = RoomParticipants.from(findByRoomId);

        // 3. 투표 참여율이 가장 높은 투표 조회
        List<RoomPlayingDetailViewResponse.CurrentVote> topParticipationVotes = voteQueryPort.findTopParticipationVotesByRoom(room, TOP_PARTICIPATION_VOTES_COUNT);

        // 4. response 구성
        return buildResponse(userId, room, book, roomParticipants, topParticipationVotes);
    }

    private RoomPlayingDetailViewResponse buildResponse(Long userId, Room room, Book book, RoomParticipants roomParticipants, List<RoomPlayingDetailViewResponse.CurrentVote> topParticipationVotes) {
        return RoomPlayingDetailViewResponse.builder()
                .isHost(roomParticipants.isHostOfRoom(userId))
                .roomId(room.getId())
                .roomName(room.getTitle())
                .roomImageUrl(room.getCategory().getImageUrl())
                .isPublic(room.isPublic())
                .progressStartDate(DateUtil.formatDate(room.getStartDate()))
                .progressEndDate(DateUtil.formatDate(room.getEndDate()))
                .category(room.getCategory().getValue())
                .roomDescription(room.getDescription())
                .memberCount(roomParticipants.calculateMemberCount())
                .recruitCount(room.getRecruitCount())
                .isbn(book.getIsbn())
                .bookTitle(book.getTitle())
                .authorName(book.getAuthorName())
                .currentPage(roomParticipants.getCurrentPageOfUser(userId))
                .userPercentage(roomParticipants.getUserPercentageOfUser(userId))
                .currentVotes(topParticipationVotes)
                .categoryColor(roomQueryPort.findAliasColorOfCategory(room.getCategory()))      // TODO : 리펙토링 대상
                .build();
    }
}
