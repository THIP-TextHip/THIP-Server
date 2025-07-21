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
        return new RoomRecruitingDetailViewResponse(
                participants.isHostOfRoom(userId),
                participants.isJoiningToRoom(userId),
                room.getId(),
                room.getTitle(),
                room.getCategory().getImageUrl(),
                room.isPublic(),
                DateUtil.formatDate(room.getStartDate()),
                DateUtil.formatDate(room.getEndDate()),
                DateUtil.formatAfterTime(room.getStartDate()),
                room.getCategory().getValue(),
                room.getDescription(),
                participants.calculateMemberCount(),
                room.getRecruitCount(),
                book.getIsbn(),
                book.getImageUrl(),
                book.getTitle(),
                book.getAuthorName(),
                book.getDescription(),
                recommendRooms
        );
    }
}
