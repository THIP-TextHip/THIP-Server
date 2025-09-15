package konkuk.thip.room.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.application.port.in.RoomShowRecruitingDetailViewUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.room.domain.RoomParticipants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomShowRecruitingDetailViewService implements RoomShowRecruitingDetailViewUseCase {

    private final static int RECOMMEND_ROOM_COUNT = 5;

    private final RoomCommandPort roomCommandPort;
    private final RoomQueryPort roomQueryPort;
    private final BookCommandPort bookCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    @Override
    @Transactional(readOnly = true)
    public RoomRecruitingDetailViewResponse getRecruitingRoomDetailView(Long userId, Long roomId) {
        // 1. Room 조회, Book 조회
        Room room = roomCommandPort.getByIdOrThrow(roomId);
        room.validateRoomRecruitExpired(); // 모집기간 지난 방 예외처리

        Book book = bookCommandPort.findById(room.getBookId());

        // 2. Room과 연관된 UserRoom 조회, RoomParticipants 일급 컬렉션 생성
        List<RoomParticipant> findByRoomId = roomParticipantCommandPort.findAllByRoomId(roomId);
        RoomParticipants roomParticipants = RoomParticipants.from(findByRoomId);

        // 3. 다른 모임방 추천
        List<RoomRecruitingDetailViewResponse.RecommendRoom> recommendRooms = roomQueryPort.findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(room, RECOMMEND_ROOM_COUNT);

        // 4. response 구성
        return buildResponse(userId, room, book, roomParticipants, recommendRooms);
    }

    private RoomRecruitingDetailViewResponse buildResponse(
            Long userId,
            Room room,
            Book book,
            RoomParticipants participants,
            List<RoomRecruitingDetailViewResponse.RecommendRoom> recommendRooms
    ) {
        return RoomRecruitingDetailViewResponse.builder()
                .isHost(participants.isHostOfRoom(userId))
                .isJoining(participants.isJoiningToRoom(userId))
                .roomId(room.getId())
                .roomName(room.getTitle())
                .roomImageUrl(room.getCategory().getImageUrl())
                .isPublic(room.isPublic())
                .progressStartDate(DateUtil.formatDate(room.getStartDate()))
                .progressEndDate(DateUtil.formatDate(room.getEndDate()))
                .recruitEndDate(DateUtil.recruitingRoomFormatAfterTime(room.getStartDate()))
                .category(room.getCategory().getValue())
                .categoryColor(roomQueryPort.findAliasColorOfCategory(room.getCategory()))      // TODO : 리펙토링 대상
                .roomDescription(room.getDescription())
                .memberCount(participants.calculateMemberCount())
                .recruitCount(room.getRecruitCount())
                .isbn(book.getIsbn())
                .bookImageUrl(book.getImageUrl())
                .bookTitle(book.getTitle())
                .authorName(book.getAuthorName())
                .bookDescription(book.getDescription())
                .publisher(book.getPublisher())
                .recommendRooms(recommendRooms)
                .build();
    }
}
