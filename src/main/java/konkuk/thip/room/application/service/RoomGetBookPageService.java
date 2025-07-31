package konkuk.thip.room.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.room.adapter.in.web.response.RoomGetBookPageResponse;
import konkuk.thip.room.application.port.in.RoomGetBookPageUseCase;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomGetBookPageService implements RoomGetBookPageUseCase {

    private final BookCommandPort bookCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final RoomParticipantValidator participantValidator;

    @Override
    public RoomGetBookPageResponse getBookPage(Long userId, Long roomId) {
        participantValidator.validateUserIsRoomMember(roomId, userId);

        Book book = bookCommandPort.findBookByRoomId(roomId);
        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(userId, roomId);

        return RoomGetBookPageResponse.of(book.getPageCount(), roomParticipant.canWriteOverview());
    }
}
